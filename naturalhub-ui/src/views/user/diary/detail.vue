<template>
  <div class="diary-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <!-- 头部操作栏 -->
      <div slot="header" class="detail-header">
        <el-button icon="el-icon-back" size="small" @click="goBack">返回</el-button>
        <div class="header-actions">
          <el-button
            v-if="diary.isArchived !== '1'"
            type="primary"
            icon="el-icon-edit"
            size="small"
            @click="handleEdit"
          >编辑</el-button>
          <el-button
            type="warning"
            :icon="diary.isArchived === '1' ? 'el-icon-folder-opened' : 'el-icon-folder'"
            size="small"
            @click="handleArchive"
          >{{ diary.isArchived === '1' ? '取消归档' : '归档' }}</el-button>
          <el-button
            type="danger"
            icon="el-icon-delete"
            size="small"
            @click="handleDelete"
          >删除</el-button>
          <el-button
            type="success"
            icon="el-icon-download"
            size="small"
            @click="handleExport"
          >导出</el-button>
        </div>
      </div>

      <!-- 标题和日期 -->
      <div class="detail-title-section">
        <h1 class="detail-title">{{ diary.title || '暂无标题' }}</h1>
        <div class="detail-meta">
          <span class="meta-item">
            <i class="el-icon-time"></i>
            {{ parseTime(diary.observationDate, '{y}年{m}月{d}日 {h}:{i}') }}
          </span>
          <span class="meta-item">
            <i class="el-icon-location"></i>
            {{ diary.location || '暂无地点' }}
          </span>
          <el-tag v-if="diary.visibility === '0'" type="info" size="medium">
            <i class="el-icon-lock"></i> 私密
          </el-tag>
          <el-tag v-else type="success" size="medium">
            <i class="el-icon-view"></i> 公开
          </el-tag>
          <el-tag v-if="diary.isArchived === '1'" type="warning" size="medium">
            <i class="el-icon-folder"></i> 已归档
          </el-tag>
        </div>
      </div>

      <!-- 基本信息 -->
      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="观察日期">
          <i class="el-icon-date"></i> {{ parseTime(diary.observationDate, '{y}-{m}-{d} {h}:{i}') }}
        </el-descriptions-item>
        <el-descriptions-item label="观察地点">
          <i class="el-icon-location-outline"></i> {{ diary.location || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="天气情况">
          <i class="el-icon-sunny"></i> {{ diary.weather || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="温度">
          <i class="el-icon-thermometer"></i> {{ diary.temperature || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="可见性">
          <el-tag :type="diary.visibility === '0' ? 'info' : 'success'" size="small">
            {{ diary.visibility === '0' ? '私密' : '公开' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="归档状态">
          <el-tag :type="diary.isArchived === '1' ? 'warning' : 'success'" size="small">
            {{ diary.isArchived === '1' ? '已归档' : '活跃' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ diary.createTime || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ diary.updateTime || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 路线信息 -->
      <div v-if="diary.routeInfo" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-guide"></i> 探索路线
        </h3>
        <div class="section-content">
          {{ diary.routeInfo }}
        </div>
      </div>

      <!-- 发现物种 -->
      <div v-if="diary.speciesFound" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-discover"></i> 发现物种
        </h3>
        <div class="section-content species-content">
          <pre>{{ diary.speciesFound }}</pre>
        </div>
      </div>

      <!-- 日志内容 -->
      <div v-if="diary.content" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-document"></i> 观察记录
        </h3>
        <div class="section-content diary-content">
          <pre>{{ diary.content }}</pre>
        </div>
      </div>

      <!-- 标签 -->
      <div v-if="diary.tags" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-collection-tag"></i> 标签
        </h3>
        <div class="tags-container">
          <el-tag
            v-for="tag in getTags(diary.tags)"
            :key="tag"
            type="primary"
            effect="plain"
            size="medium"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <!-- 图片展示 -->
      <div v-if="imageList.length > 0" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-picture"></i> 观察图片
        </h3>
        <div class="image-gallery">
          <el-image
            v-for="(url, index) in imageList"
            :key="index"
            :src="url"
            :preview-src-list="imageList"
            fit="cover"
            class="gallery-image"
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </div>
      </div>

      <!-- 备注信息 -->
      <div v-if="diary.remark" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-chat-line-square"></i> 备注
        </h3>
        <div class="section-content">
          {{ diary.remark }}
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-data-line"></i> 统计信息
        </h3>
        <el-row :gutter="20" class="stats-row">
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-icon" style="background: #409EFF;">
                <i class="el-icon-view"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ diary.viewCount || 0 }}</div>
                <div class="stat-label">浏览次数</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-icon" style="background: #67C23A;">
                <i class="el-icon-thumb"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ diary.likeCount || 0 }}</div>
                <div class="stat-label">点赞数</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-icon" style="background: #E6A23C;">
                <i class="el-icon-star-off"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ diary.favoriteCount || 0 }}</div>
                <div class="stat-label">收藏数</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 操作历史 -->
      <div class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-time"></i> 操作历史
        </h3>
        <el-timeline>
          <el-timeline-item
            timestamp="创建日志"
            color="#67C23A"
          >
            <p>创建时间：{{ diary.createTime || '暂无' }}</p>
            <p v-if="diary.createBy">创建人：{{ diary.createBy }}</p>
          </el-timeline-item>
          <el-timeline-item
            v-if="diary.updateTime && diary.updateTime !== diary.createTime"
            timestamp="更新日志"
            color="#409EFF"
          >
            <p>更新时间：{{ diary.updateTime }}</p>
            <p v-if="diary.updateBy">更新人：{{ diary.updateBy }}</p>
          </el-timeline-item>
          <el-timeline-item
            v-if="diary.isArchived === '1'"
            timestamp="归档日志"
            color="#E6A23C"
          >
            <p>归档时间：{{ diary.archiveTime || '暂无' }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getDiary, archiveDiary, delDiary } from "@/api/user/diary";

export default {
  name: "DiaryDetail",
  data() {
    return {
      loading: true,
      diary: {},
      imageList: []
    };
  },
  created() {
    this.getDetail();
  },
  methods: {
    getDetail() {
      const diaryId = this.$route.query.id;
      if (!diaryId) {
        this.$modal.msgError("参数错误");
        this.goBack();
        return;
      }
      
      getDiary(diaryId).then(response => {
        this.diary = response.data;
        
        // 处理图片列表
        if (this.diary.images) {
          this.imageList = this.diary.images.split(',');
        }
        
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    goBack() {
      this.$router.go(-1);
    },
    handleEdit() {
      if (this.diary.isArchived === '1') {
        this.$modal.msgWarning('该日志已归档，无法编辑！');
        return;
      }
      this.$router.push({
        path: "/user/diary/create",
        query: { id: this.diary.diaryId }
      });
    },
    handleArchive() {
      const isArchived = this.diary.isArchived === '1' ? '0' : '1';
      const text = isArchived === '1' ? '归档' : '取消归档';
      
      this.$modal.confirm('是否确认' + text + '该日志？').then(() => {
        return archiveDiary(this.diary.diaryId, isArchived);
      }).then(() => {
        this.$modal.msgSuccess(text + '成功');
        this.getDetail();
      }).catch(() => {});
    },
    handleDelete() {
      this.$modal.confirm('是否确认删除该观察日志？删除后将无法恢复！', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delDiary(this.diary.diaryId);
      }).then(() => {
        this.$modal.msgSuccess('删除成功');
        this.$router.push('/user/diary/list');
      }).catch(() => {});
    },
    handleExport() {
      this.download('user/diary/export', {
        diaryId: this.diary.diaryId
      }, `diary_${this.diary.diaryId}_${new Date().getTime()}.pdf`);
    },
    getTags(tags) {
      if (!tags) return [];
      return tags.split(',').filter(tag => tag.trim());
    }
  }
};
</script>

<style lang="scss" scoped>
.diary-detail-container {
  padding: 20px;

  .detail-card {
    max-width: 1200px;
    margin: 0 auto;

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-actions {
        .el-button {
          margin-left: 10px;
        }
      }
    }

    .detail-title-section {
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 2px solid #f0f0f0;

      .detail-title {
        font-size: 32px;
        font-weight: bold;
        color: #303133;
        margin: 0 0 15px 0;
        line-height: 1.4;
      }

      .detail-meta {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 15px;
        color: #606266;
        font-size: 14px;

        .meta-item {
          display: flex;
          align-items: center;

          i {
            margin-right: 5px;
            color: #909399;
          }
        }
      }
    }

    .detail-info {
      margin-bottom: 30px;

      ::v-deep .el-descriptions-item__label {
        font-weight: bold;
      }
    }

    .detail-section {
      margin-bottom: 30px;

      .section-title {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
        border-left: 4px solid #409EFF;
        padding-left: 10px;
        margin-bottom: 15px;

        i {
          color: #409EFF;
          margin-right: 8px;
        }
      }

      .section-content {
        padding: 20px;
        background: #f5f7fa;
        border-radius: 8px;
        line-height: 1.8;
        color: #606266;

        pre {
          white-space: pre-wrap;
          word-wrap: break-word;
          font-family: inherit;
          margin: 0;
        }

        &.species-content {
          background: linear-gradient(135deg, #f5f7fa 0%, #e8f4f8 100%);
        }

        &.diary-content {
          font-size: 15px;
          min-height: 200px;
        }
      }

      .tags-container {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;

        .el-tag {
          font-size: 14px;
          padding: 8px 16px;
        }
      }

      .image-gallery {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 20px;

        .gallery-image {
          width: 100%;
          height: 250px;
          border-radius: 8px;
          cursor: pointer;
          transition: transform 0.3s;

          &:hover {
            transform: scale(1.05);
          }

          .image-slot {
            display: flex;
            justify-content: center;
            align-items: center;
            width: 100%;
            height: 100%;
            background: #f5f7fa;
            color: #909399;

            i {
              font-size: 48px;
            }
          }
        }
      }

      .stats-row {
        .stat-card {
          display: flex;
          align-items: center;
          padding: 20px;
          background: #fff;
          border-radius: 8px;
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
          transition: transform 0.3s;

          &:hover {
            transform: translateY(-5px);
          }

          .stat-icon {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 15px;

            i {
              font-size: 28px;
              color: #fff;
            }
          }

          .stat-info {
            flex: 1;

            .stat-value {
              font-size: 28px;
              font-weight: bold;
              color: #303133;
              line-height: 1;
              margin-bottom: 5px;
            }

            .stat-label {
              font-size: 14px;
              color: #909399;
            }
          }
        }
      }
    }
  }
}

// 移动端适配
@media screen and (max-width: 768px) {
  .diary-detail-container {
    padding: 10px;

    .detail-card {
      .detail-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;

        .header-actions {
          width: 100%;
          display: flex;
          flex-wrap: wrap;
          gap: 5px;

          .el-button {
            margin-left: 0;
            flex: 1;
            min-width: calc(50% - 5px);
          }
        }
      }

      .detail-title-section {
        .detail-title {
          font-size: 24px;
        }

        .detail-meta {
          font-size: 13px;
        }
      }

      .detail-section {
        .image-gallery {
          grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
          gap: 10px;

          .gallery-image {
            height: 150px;
          }
        }

        .stats-row {
          .el-col {
            margin-bottom: 10px;
          }
        }
      }
    }
  }
}
</style>
