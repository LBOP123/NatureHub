<template>
  <div class="app-container identification-square">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="关键词" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="搜索标题或地点"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="待鉴定" value="pending" />
          <el-option label="已回答" value="answered" />
          <el-option label="已解决" value="resolved" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="20">
      <el-col
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
        v-for="item in identificationList"
        :key="item.identificationId"
        style="margin-bottom: 20px"
      >
        <el-card :body-style="{ padding: '0px' }" shadow="hover" class="identification-card">
          <div class="card-image" @click="handleDetail(item)">
            <img v-if="item.images" :src="getFirstImage(item.images)" class="image">
            <img v-else src="@/assets/images/profile.jpg" class="image">
            <div class="status-badge">
              <el-tag v-if="item.status === 'pending'" type="warning" size="small">待鉴定</el-tag>
              <el-tag v-else-if="item.status === 'answered'" type="primary" size="small">已回答</el-tag>
              <el-tag v-else-if="item.status === 'resolved'" type="success" size="small">已解决</el-tag>
            </div>
          </div>
          <div class="card-content">
            <div class="title" @click="handleDetail(item)">{{ item.title }}</div>
            <div class="info">
              <span><i class="el-icon-location"></i> {{ item.location }}</span>
            </div>
            <div class="info">
              <span><i class="el-icon-time"></i> {{ parseTime(item.observationTime, '{y}-{m}-{d}') }}</span>
            </div>
            <div class="stats">
              <span><i class="el-icon-view"></i> {{ item.viewCount }}</span>
              <span><i class="el-icon-chat-dot-round"></i> {{ item.answerCount }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listIdentificationSquare } from "@/api/user/identification";

export default {
  name: "IdentificationSquare",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 鉴定求助列表
      identificationList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        title: null,
        status: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      listIdentificationSquare(this.queryParams).then(response => {
        this.identificationList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 查看详情 */
    handleDetail(row) {
      this.$router.push({
        path: "/user/identification/detail",
        query: { id: row.identificationId }
      });
    },
    /** 获取第一张图片 */
    getFirstImage(images) {
      if (!images) return '';
      const imageArray = images.split(',');
      return imageArray[0];
    }
  }
};
</script>

<style scoped>
.identification-square {
  padding: 20px;
}

.identification-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.identification-card:hover {
  transform: translateY(-5px);
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-image .image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}

.card-content {
  padding: 15px;
}

.title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.title:hover {
  color: #409EFF;
}

.info {
  font-size: 13px;
  color: #909399;
  margin-bottom: 5px;
}

.info i {
  margin-right: 5px;
}

.stats {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #EBEEF5;
  font-size: 13px;
  color: #909399;
}

.stats span {
  display: flex;
  align-items: center;
}

.stats i {
  margin-right: 5px;
}
</style>
