<template>
  <div class="community-container">
    <!-- 顶部分类导航 -->
    <div class="category-tabs">
      <el-radio-group v-model="queryParams.category" @change="handleQuery" size="medium">
        <el-radio-button label="">全部话题</el-radio-button>
        <el-radio-button
          v-for="item in categoryList"
          :key="item.categoryCode"
          :label="item.categoryCode"
        >
          {{ item.categoryIcon }} {{ item.categoryName }}
        </el-radio-button>
      </el-radio-group>
      <el-button type="primary" icon="el-icon-edit" @click="handleCreate">发布话题</el-button>
    </div>

    <!-- 精华/置顶筛选 -->
    <div class="filter-bar">
      <el-checkbox-group v-model="filterOptions" @change="handleQuery">
        <el-checkbox label="essence">只看精华</el-checkbox>
        <el-checkbox label="top">只看置顶</el-checkbox>
      </el-checkbox-group>
      <el-input
        v-model="queryParams.title"
        placeholder="搜索话题标题"
        clearable
        style="width: 300px;"
        @keyup.enter.native="handleQuery"
      >
        <el-button slot="append" icon="el-icon-search" @click="handleQuery"></el-button>
      </el-input>
    </div>

    <!-- 话题列表 -->
    <div class="topic-list">
      <el-card
        v-for="topic in topicList"
        :key="topic.topicId"
        class="topic-card"
        shadow="hover"
        @click.native="handleView(topic)"
      >
        <div class="topic-header">
          <div class="topic-badges">
            <el-tag v-if="topic.isTop === '1'" type="danger" size="mini" effect="dark">置顶</el-tag>
            <el-tag v-if="topic.isEssence === '1'" type="warning" size="mini" effect="dark">精华</el-tag>
            <el-tag :type="getCategoryType(topic.category)" size="mini">{{ getCategoryName(topic.category) }}</el-tag>
            <el-tag v-if="topic.sourceType === 1" type="success" size="mini"><i class="el-icon-view"></i> 来自观察记录</el-tag>
            <el-tag v-else-if="topic.sourceType === 2" type="warning" size="mini"><i class="el-icon-search"></i> 来自物种鉴定</el-tag>
            <el-tag v-else-if="topic.sourceType === 3" type="primary" size="mini"><i class="el-icon-map-location"></i> 来自野外调查</el-tag>
            <el-tag v-else-if="topic.sourceType === 4" type="info" size="mini"><i class="el-icon-notebook-2"></i> 来自观察日志</el-tag>
          </div>
          <div class="topic-author">
            <el-avatar :size="32" icon="el-icon-user-solid" class="topic-author-avatar" @click.stop="goToUserProfile(topic.userId)"></el-avatar>
            <span class="author-name" @click.stop="goToUserProfile(topic.userId)">{{ topic.userName }}</span>
            <span class="topic-time">{{ parseTime(topic.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
        </div>

        <h3 class="topic-title">{{ topic.title }}</h3>

        <div class="topic-content">
          <p>{{ truncate(topic.content, 200) }}</p>
        </div>

        <div class="topic-tags" v-if="topic.tags">
          <el-tag v-for="tag in topic.tags.split(',')" :key="tag" size="mini" type="info" effect="plain"># {{ tag }}</el-tag>
        </div>

        <div class="topic-stats">
          <span><i class="el-icon-view"></i> {{ topic.viewCount }}</span>
          <span :class="{ active: topic.isLiked }" @click.stop="handleLike(topic)">
            <i class="el-icon-thumb"></i> {{ topic.likeCount }}
          </span>
          <span><i class="el-icon-chat-dot-round"></i> {{ topic.commentCount }}</span>
          <span :class="{ active: topic.isCollected }" @click.stop="handleCollect(topic)">
            <i class="el-icon-star-off"></i> {{ topic.collectCount }}
          </span>
<!--          <span v-if="topic.sourceId" class="view-source" @click.stop="viewSource(topic)">
            <i class="el-icon-link"></i> 查看原始记录
          </span>-->
        </div>
      </el-card>

      <el-empty v-if="topicList.length === 0" description="暂无话题，快来发布第一个吧！"></el-empty>
    </div>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import {
  listTopic,
  likeTopic, unlikeTopic, collectTopic, uncollectTopic
} from '@/api/user/community'
import { listAllCategory } from '@/api/community/category'
import { getDicts } from '@/api/system/dict/data'

export default {
  name: 'CommunityList',
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        category: '',
        title: null,
        auditStatus: '1'
      },
      filterOptions: [],
      topicList: [],
      communityCategoryOptions: [],
      categoryList: [],
      total: 0
    }
  },
  created() {
    this.getDicts('nh_community_category_type').then(res => {
      this.communityCategoryOptions = res.data || []
    })
    this.getList()
    this.getCategoryList()
  },
  methods: {
    getDicts(dictType) {
      return getDicts(dictType)
    },
    getCategoryList() {
      listAllCategory().then(response => {
        this.categoryList = response.data
      })
    },
    getList() {
      const params = { ...this.queryParams }
      if (this.filterOptions.includes('essence')) params.isEssence = '1'
      if (this.filterOptions.includes('top')) params.isTop = '1'
      listTopic(params).then(response => {
        this.topicList = response.rows
        this.total = response.total
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleCreate() {
      this.$router.push({ path: '/user/community/create' })
    },
    handleView(topic) {
      this.$router.push({ path: '/user/community/detail', query: { id: topic.topicId } })
    },
    getCategoryName(val) {
      const item = this.communityCategoryOptions.find(d => d.dictValue == val);
      return item ? item.dictLabel : "未分类";
    },
    getCategoryType(category) {
      const map = { 'species_science': 'success', 'field_explore': 'primary', 'identify_help': 'warning' }
      return map[category] || ''
    },
    truncate(value, length) {
      if (!value) return ''
      if (value.length <= length) return value
      return value.substring(0, length) + '...'
    },
    goToUserProfile(userId) {
      if (!userId) return
      this.$router.push({ path: '/user/myProfile', query: { userId } })
    },
    handleLike(topic) {
      if (topic.isLiked) {
        unlikeTopic(topic.topicId).then(() => { topic.isLiked = false; topic.likeCount--; this.$message.success('已取消点赞') })
      } else {
        likeTopic(topic.topicId).then(() => { topic.isLiked = true; topic.likeCount++; this.$message.success('点赞成功') })
      }
    },
    handleCollect(topic) {
      if (topic.isCollected) {
        uncollectTopic(topic.topicId).then(() => { topic.isCollected = false; topic.collectCount--; this.$message.success('已取消收藏') })
      } else {
        collectTopic(topic.topicId).then(() => { topic.isCollected = true; topic.collectCount++; this.$message.success('收藏成功') })
      }
    },
    viewSource(topic) {
      const routeMap = {
        'observation': '/user/observation/detail',
        'identification': '/user/identification/detail',
        'survey': '/user/survey/detail',
        'diary': '/user/diary/detail'
      }
      const path = routeMap[topic.sourceType]
      if (path && topic.sourceId) {
        this.$router.push({ path, query: { id: topic.sourceId } })
      } else {
        this.$message.warning('无法找到原始记录')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.community-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.category-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background: white;
  border-radius: 8px;
}

.topic-list {
  display: grid;
  gap: 16px;
}

.topic-card {
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  }

  .topic-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .topic-badges { display: flex; gap: 8px; flex-wrap: wrap; }

    .topic-author {
      display: flex;
      align-items: center;
      gap: 10px;
      .topic-author-avatar { cursor: pointer; }
      .author-name { font-weight: 500; color: #606266; cursor: pointer; }
      .topic-time { font-size: 13px; color: #909399; }
    }
  }

  .topic-title {
    font-size: 20px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 12px 0;
    line-height: 1.4;
  }

  .topic-content {
    margin-bottom: 12px;
    p { color: #606266; line-height: 1.6; margin: 0; }
  }

  .topic-tags {
    margin-bottom: 12px;
    .el-tag { margin-right: 8px; }
  }

  .topic-stats {
    display: flex;
    gap: 24px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #909399;
      font-size: 14px;
      cursor: pointer;
      transition: color 0.3s;
      &:hover { color: #409EFF; }
      &.active { color: #409EFF; }
      i { font-size: 16px; }
    }

    .view-source {
      color: #67C23A !important;
      font-weight: 500;
      &:hover { color: #85ce61 !important; }
    }
  }
}

@media (max-width: 768px) {
  .category-tabs {
    flex-direction: column;
    gap: 12px;
    .el-radio-group { width: 100%; }
  }
  .filter-bar {
    flex-direction: column;
    gap: 12px;
    .el-input { width: 100% !important; }
  }
}
</style>
