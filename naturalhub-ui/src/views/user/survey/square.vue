<template>
  <div class="app-container survey-square">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="关键词" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="搜索标题或地点"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生境类型" prop="habitatType">
        <el-select v-model="queryParams.habitatType" placeholder="请选择生境类型" clearable>
          <el-option label="森林" value="forest" />
          <el-option label="草地" value="grassland" />
          <el-option label="湿地" value="wetland" />
          <el-option label="河流" value="river" />
          <el-option label="湖泊" value="lake" />
          <el-option label="海洋" value="ocean" />
          <el-option label="山地" value="mountain" />
          <el-option label="农田" value="farmland" />
          <el-option label="城市" value="urban" />
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
        v-for="item in surveyList"
        :key="item.surveyId"
        style="margin-bottom: 20px"
      >
        <el-card :body-style="{ padding: '0px' }" shadow="hover" class="survey-card">
          <div class="card-image" @click="handleDetail(item)">
            <img v-if="item.images" :src="getFirstImage(item.images)" class="image">
            <img v-else src="@/assets/images/profile.jpg" class="image">
            <div class="habitat-badge">
              <el-tag size="small" type="success">{{ item.habitatType }}</el-tag>
            </div>
          </div>
          <div class="card-content">
            <div class="title" @click="handleDetail(item)">{{ item.title }}</div>
            <div class="info">
              <span><i class="el-icon-location"></i> {{ item.location }}</span>
            </div>
            <div class="info">
              <span><i class="el-icon-date"></i> {{ parseTime(item.surveyDate, '{y}-{m}-{d}') }}</span>
            </div>
            <div class="stats">
              <span><i class="el-icon-data-analysis"></i> {{ item.speciesCount }} 种</span>
              <span><i class="el-icon-user"></i> {{ item.userName }}</span>
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
import { listSurveySquare } from "@/api/user/survey";

export default {
  name: "SurveySquare",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      surveyList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        title: null,
        habitatType: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listSurveySquare(this.queryParams).then(response => {
        this.surveyList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleDetail(row) {
      this.$router.push({
        path: "/user/survey/detail",
        query: { id: row.surveyId }
      });
    },
    getFirstImage(images) {
      if (!images) return '';
      const imageArray = images.split(',');
      return imageArray[0];
    }
  }
};
</script>

<style scoped>
.survey-square {
  padding: 20px;
}

.survey-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.survey-card:hover {
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

.habitat-badge {
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
