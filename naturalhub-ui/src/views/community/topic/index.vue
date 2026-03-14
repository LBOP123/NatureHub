<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="话题标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入话题标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="话题分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择话题分类" clearable>
          <el-option v-for="dict in categoryTypeOptions" :key="dict.dictValue" :label="dict.dictLabel" :value="parseInt(dict.dictValue)" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否置顶" prop="isTop">
        <el-select v-model="queryParams.isTop" placeholder="请选择是否置顶" clearable>
          <el-option label="是" value="1" />
          <el-option label="否" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否精华" prop="isEssence">
        <el-select v-model="queryParams.isEssence" placeholder="请选择是否精华" clearable>
          <el-option label="是" value="1" />
          <el-option label="否" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="关闭" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['community:topic:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['community:topic:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="topicList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="话题ID" align="center" prop="topicId" width="80" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="200">
        <template slot-scope="scope">
          <el-link type="primary" @click="handleView(scope.row)">{{ scope.row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="分类" align="center" prop="category" width="100">
        <template slot-scope="scope">
          <dict-tag :options="categoryTypeOptions" :value="String(scope.row.category)" />
        </template>
      </el-table-column>
      <el-table-column label="发布者" align="center" prop="userName" width="100" />
      <el-table-column label="统计" align="center" width="200">
        <template slot-scope="scope">
          <div class="stats-row">
            <span><i class="el-icon-view"></i> {{ scope.row.viewCount }}</span>
            <span><i class="el-icon-thumb"></i> {{ scope.row.likeCount }}</span>
            <span><i class="el-icon-chat-dot-round"></i> {{ scope.row.commentCount }}</span>
            <span><i class="el-icon-star-off"></i> {{ scope.row.collectCount }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="热度分数" align="center" prop="hotScore" width="100" />
      <el-table-column label="标记" align="center" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isTop === '1'" type="danger" size="mini">置顶</el-tag>
          <el-tag v-if="scope.row.isEssence === '1'" type="warning" size="mini">精华</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="danger">关闭</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="280">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            :icon="scope.row.isTop === '1' ? 'el-icon-bottom' : 'el-icon-top'"
            @click="handleTop(scope.row)"
            v-hasPermi="['community:topic:edit']"
          >{{ scope.row.isTop === '1' ? '取消置顶' : '置顶' }}</el-button>
          <el-button
            size="mini"
            type="text"
            :icon="scope.row.isEssence === '1' ? 'el-icon-star-off' : 'el-icon-star-on'"
            @click="handleEssence(scope.row)"
            v-hasPermi="['community:topic:edit']"
          >{{ scope.row.isEssence === '1' ? '取消精华' : '设为精华' }}</el-button>
          <el-button
            size="mini"
            type="text"
            :icon="scope.row.status === '1' ? 'el-icon-unlock' : 'el-icon-lock'"
            @click="handleStatus(scope.row)"
            v-hasPermi="['community:topic:edit']"
          >{{ scope.row.status === '1' ? '开启' : '屏蔽' }}</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['community:topic:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 话题详情对话框 -->
    <el-dialog title="话题详情" :visible.sync="detailVisible" width="900px" append-to-body>
      <div class="topic-detail" v-if="currentTopic">
        <div class="detail-header">
          <h2>{{ currentTopic.title }}</h2>
          <div class="detail-badges">
            <el-tag v-if="currentTopic.isTop === '1'" type="danger">置顶</el-tag>
            <el-tag v-if="currentTopic.isEssence === '1'" type="warning">精华</el-tag>
            <el-tag v-if="currentTopic.status === '1'" type="danger">已关闭</el-tag>
            <dict-tag :options="categoryTypeOptions" :value="String(currentTopic.category)" />
          </div>
        </div>

        <div class="detail-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="发布者">{{ currentTopic.userName }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ parseTime(currentTopic.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="浏览次数">{{ currentTopic.viewCount }}</el-descriptions-item>
            <el-descriptions-item label="点赞数">{{ currentTopic.likeCount }}</el-descriptions-item>
            <el-descriptions-item label="评论数">{{ currentTopic.commentCount }}</el-descriptions-item>
            <el-descriptions-item label="收藏数">{{ currentTopic.collectCount }}</el-descriptions-item>
            <el-descriptions-item label="热度分数">{{ currentTopic.hotScore }}</el-descriptions-item>
            <el-descriptions-item label="标签">{{ currentTopic.tags || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-content">
          <h4>话题内容</h4>
          <div class="content-text">{{ currentTopic.content }}</div>
        </div>

        <div class="detail-actions">
          <el-button
            type="primary"
            :icon="currentTopic.isTop === '1' ? 'el-icon-bottom' : 'el-icon-top'"
            @click="handleTop(currentTopic)"
          >{{ currentTopic.isTop === '1' ? '取消置顶' : '置顶话题' }}</el-button>
          <el-button
            type="warning"
            :icon="currentTopic.isEssence === '1' ? 'el-icon-star-off' : 'el-icon-star-on'"
            @click="handleEssence(currentTopic)"
          >{{ currentTopic.isEssence === '1' ? '取消精华' : '设为精华' }}</el-button>
          <el-button
            :type="currentTopic.status === '1' ? 'success' : 'danger'"
            :icon="currentTopic.status === '1' ? 'el-icon-unlock' : 'el-icon-lock'"
            @click="handleStatus(currentTopic)"
          >{{ currentTopic.status === '1' ? '开启话题' : '关闭话题' }}</el-button>
          <el-button
            type="danger"
            icon="el-icon-delete"
            @click="handleDelete(currentTopic)"
          >删除话题</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTopic, getTopic, delTopic, exportTopic, setTopicTop, setTopicEssence, setTopicStatus } from "@/api/community/topic"

export default {
  name: "CommunityTopic",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      topicList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        category: null,
        isTop: null,
        isEssence: null,
        status: null
      },
      categoryTypeOptions: [],
      categoryTypeOptions: [],
      categoryTypeOptions: [],
      detailVisible: false,
      currentTopic: null
    }
  },
  created() {
    this.getDicts('nh_community_category_type').then(res => { this.categoryTypeOptions = res.data; });
    this.getDicts('nh_community_category_type').then(res => { this.categoryTypeOptions = res.data; });
    this.getDicts('nh_community_category_type').then(res => { this.categoryTypeOptions = res.data; });
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listTopic(this.queryParams).then(response => {
        this.topicList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.topicId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleView(row) {
      getTopic(row.topicId).then(response => {
        this.currentTopic = response.data
        this.detailVisible = true
      })
    },
    handleTop(row) {
      const isTop = row.isTop === '1' ? '0' : '1'
      const text = isTop === '1' ? '置顶' : '取消置顶'
      this.$modal.confirm('是否确认' + text + '该话题？').then(() => {
        return setTopicTop(row.topicId, isTop)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess(text + '成功')
        if (this.detailVisible) {
          this.currentTopic.isTop = isTop
        }
      }).catch(() => {})
    },
    handleEssence(row) {
      const isEssence = row.isEssence === '1' ? '0' : '1'
      const text = isEssence === '1' ? '设为精华' : '取消精华'
      this.$modal.confirm('是否确认' + text + '该话题？').then(() => {
        return setTopicEssence(row.topicId, isEssence)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess(text + '成功')
        if (this.detailVisible) {
          this.currentTopic.isEssence = isEssence
        }
      }).catch(() => {})
    },
    handleStatus(row) {
      const status = row.status === '1' ? '0' : '1'
      const text = status === '1' ? '关闭' : '开启'
      this.$modal.confirm('是否确认' + text + '该话题？' + (status === '1' ? '关闭后用户端将无法查看。' : '')).then(() => {
        return setTopicStatus(row.topicId, status)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess(text + '成功')
        if (this.detailVisible) {
          this.currentTopic.status = status
          if (status === '1') {
            this.detailVisible = false
          }
        }
      }).catch(() => {})
    },
    handleDelete(row) {
      const topicIds = row.topicId || this.ids
      this.$modal.confirm('是否确认删除话题编号为"' + topicIds + '"的数据项？').then(() => {
        return delTopic(topicIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
        if (this.detailVisible) {
          this.detailVisible = false
        }
      }).catch(() => {})
    },
    handleExport() {
      this.download('community/topic/export', {
        ...this.queryParams
      }, `社群话题_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style lang="scss" scoped>
.stats-row {
  display: flex;
  justify-content: space-around;
  font-size: 12px;

  span {
    display: flex;
    align-items: center;
    gap: 2px;
  }
}

.topic-detail {
  .detail-header {
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 2px solid #f0f0f0;

    h2 {
      margin: 0 0 10px 0;
      color: #2c3e50;
    }

    .detail-badges {
      display: flex;
      gap: 8px;
    }
  }

  .detail-info {
    margin-bottom: 20px;
  }

  .detail-content {
    margin-bottom: 20px;

    h4 {
      color: #2c3e50;
      margin: 0 0 10px 0;
    }

    .content-text {
      padding: 15px;
      background: #f5f7fa;
      border-radius: 4px;
      line-height: 1.8;
      white-space: pre-wrap;
      color: #606266;
    }
  }

  .detail-actions {
    display: flex;
    gap: 10px;
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>
