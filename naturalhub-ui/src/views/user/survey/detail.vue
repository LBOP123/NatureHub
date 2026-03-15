  <template>
    <div class="survey-detail-container">
      <el-card v-loading="loading" class="detail-card">
        <!-- 头部操作栏 -->
        <div slot="header" class="detail-header">
          <el-button icon="el-icon-back" size="small" @click="goBack">返回</el-button>
          <div class="header-actions">
            <el-button
              v-if="survey.auditStatus === 0 || survey.auditStatus === 3"
              type="primary"
              icon="el-icon-edit"
              size="small"
              @click="handleEdit"
            >编辑</el-button>
            <el-button
              v-if="survey.auditStatus === 2 && survey.isShared === 0"
              type="success"
              icon="el-icon-share"
              size="small"
              @click="handleShare"
            >分享到社群</el-button>
            <el-button
              type="danger"
              icon="el-icon-delete"
              size="small"
              @click="handleDelete"
            >删除</el-button>
          </div>
        </div>

        <!-- 标题和状态 -->
        <div class="detail-title-section">
          <h1 class="detail-title">{{ survey.title || '暂无标题' }}</h1>
          <div class="detail-status">
            <dict-tag :options="auditStatusOptions" :value="String(survey.auditStatus)" size="medium" />
            <span v-if="survey.auditStatus === 3" class="reject-reason">
              <el-popover placement="top" width="300" trigger="hover">
                <div class="reject-info">
                  <p><strong>驳回原因：</strong></p>
                  <p>{{ survey.auditRemark || '暂无说明' }}</p>
                  <p class="reject-time">驳回时间：{{ survey.auditTime || '暂无' }}</p>
                </div>
                <el-button slot="reference" type="text" size="small">
                  <i class="el-icon-warning"></i> 查看驳回原因
                </el-button>
              </el-popover>
            </span>
          </div>
        </div>

        <!-- 基本信息 -->
        <el-descriptions :column="2" border class="detail-info">
          <el-descriptions-item label="调查日期">
            <i class="el-icon-date"></i> {{ parseTime(survey.surveyDate, '{y}-{m}-{d}') }}
          </el-descriptions-item>
          <el-descriptions-item label="调查地点">
            <i class="el-icon-location"></i> {{ survey.location || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item label="生境类型">
            <el-tag type="success" size="small">{{ survey.habitatType || '暂无' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="调查方法">
            {{ survey.surveyMethod || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ parseTime(survey.startTime, '{y}-{m}-{d} {h}:{i}') }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ parseTime(survey.endTime, '{y}-{m}-{d} {h}:{i}') }}
          </el-descriptions-item>
          <el-descriptions-item label="天气情况">
            {{ survey.weather || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item label="温度">
            {{ survey.temperature || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item label="发现物种数">
            <el-tag type="primary" size="small">{{ survey.speciesCount || 0 }} 种</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="团队成员">
            {{ survey.teamMembers || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="是否分享">
            <el-tag :type="survey.isShared === 1 ? 'success' : 'info'" size="small">
              {{ survey.isShared === 1 ? '已分享' : '未分享' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="经纬度" v-if="survey.latitude && survey.longitude">
            <el-button type="text" size="small" @click="showMap">
              {{ survey.latitude }}, {{ survey.longitude }}
              <i class="el-icon-map-location"></i>
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ survey.createTime || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ survey.updateTime || '暂无' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 调查描述 -->
        <div v-if="survey.description" class="detail-section">
          <h3 class="section-title">
            <i class="el-icon-document"></i> 调查描述
          </h3>
          <div class="section-content">
            {{ survey.description }}
          </div>
        </div>

        <!-- 主要发现 -->
        <div v-if="survey.findings" class="detail-section">
          <h3 class="section-title">
            <i class="el-icon-data-analysis"></i> 主要发现
          </h3>
          <div class="section-content">
            {{ survey.findings }}
          </div>
        </div>

        <!-- 图片展示 -->
        <div v-if="imageList.length > 0" class="detail-section">
          <h3 class="section-title">
            <i class="el-icon-picture"></i> 调查图片
          </h3>
          <div class="image-gallery">
            <el-image
              v-for="(url, index) in imageList"
              :key="index"
              :src="url"
              :preview-src-list="imageList"
              fit="cover"
              class="gallery-image"
            />
          </div>
        </div>

        <!-- 审核信息 -->
        <div v-if="survey.auditStatus !== 0" class="detail-section">
          <h3 class="section-title">
            <i class="el-icon-s-check"></i> 审核信息
          </h3>
          <el-timeline>
            <el-timeline-item
              timestamp="提交审核"
              :color="survey.auditStatus === 0 ? '#409EFF' : '#67C23A'"
            >
              <p>提交时间：{{ survey.createTime || '暂无' }}</p>
            </el-timeline-item>
            <el-timeline-item
              v-if="survey.auditStatus === 2"
              timestamp="审核通过"
              color="#67C23A"
            >
              <p>审核时间：{{ survey.auditTime || '暂无' }}</p>
              <p v-if="survey.auditBy">审核人：{{ survey.auditBy }}</p>
              <p v-if="survey.auditRemark">审核意见：{{ survey.auditRemark }}</p>
            </el-timeline-item>
            <el-timeline-item
              v-if="survey.auditStatus === 3"
              timestamp="审核驳回"
              color="#F56C6C"
            >
              <p>驳回时间：{{ survey.auditTime || '暂无' }}</p>
              <p v-if="survey.auditBy">审核人：{{ survey.auditBy }}</p>
              <p v-if="survey.auditRemark">驳回原因：{{ survey.auditRemark }}</p>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>

      <!-- 地图对话框 -->
      <el-dialog title="调查位置" :visible.sync="mapDialogVisible" width="800px" append-to-body>
        <div id="map-container" style="height: 500px;"></div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="mapDialogVisible = false">关 闭</el-button>
        </div>
      </el-dialog>
    </div>
  </template>

  <script>
  import { getSurvey, delSurvey, shareSurvey } from "@/api/user/survey";

  export default {
    name: "SurveyDetail",
    data() {
      return {
        loading: true,
        auditStatusOptions: [],
        survey: {},
        imageList: [],
        mapDialogVisible: false
      };
    },
    computed: {
      isOwner() {
        return this.survey.userId === this.$store.state.user.userId;
      }
    },
    created() {
      this.getDicts('nh_observation_audit_status').then(res => { this.auditStatusOptions = res.data || [] })
      this.getDetail();
    },
    methods: {
      getDetail() {
        const surveyId = this.$route.query.id;
        getSurvey(surveyId).then(response => {
          this.survey = response.data;

          if (this.survey.images) {
            this.imageList = this.survey.images.split(',');
          }

          this.loading = false;
        });
      },
      goBack() {
        this.$router.go(-1);
      },
      handleEdit() {
        this.$router.push({
          path: "/user/survey/create",
          query: { id: this.survey.surveyId }
        });
      },
      handleShare() {
        this.$modal.confirm('确认分享到社群？').then(() => {
          return shareSurvey(this.survey.surveyId);
        }).then(() => {
          this.$modal.msgSuccess("分享成功");
          this.getDetail();
        }).catch(() => {});
      },
      handleDelete() {
        this.$modal.confirm('是否确认删除该调查记录?', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          return delSurvey(this.survey.surveyId);
        }).then(() => {
          this.$modal.msgSuccess('删除成功');
          this.$router.push('/user/survey/list');
        });
      },
      showMap() {
        this.mapDialogVisible = true;
        this.$nextTick(() => {
          this.initMap();
        });
      },
      initMap() {
        const mapContainer = document.getElementById('map-container');
        if (mapContainer) {
          mapContainer.innerHTML = `
            <div style="text-align: center; padding: 50px;">
              <p>经度：${this.survey.longitude || '暂无'}</p>
              <p>纬度：${this.survey.latitude || '暂无'}</p>
              <p style="color: #909399; margin-top: 20px;">提示：集成地图API后可显示地图</p>
            </div>
          `;
        }
      },
      getAuditStatusText(status) {
        const item = this.auditStatusOptions.find(d => d.dictValue == status);
        return item ? item.dictLabel : '';
      },
      getAuditStatusType(status) {
        switch (status) {
          case 0: return '';
          case 1: return 'warning';
          case 2: return 'success';
          case 3: return 'danger';
          default: return '';
        }
      }
    }
  };
  </script>

  <style lang="scss" scoped>
  .survey-detail-container {
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
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          margin: 0 0 15px 0;
        }
      }

      .detail-info {
        margin-bottom: 30px;
      }

      .detail-section {
        margin-bottom: 30px;

        .section-title {
          font-size: 18px;
          font-weight: bold;
          border-left: 4px solid #409EFF;
          padding-left: 10px;
          margin-bottom: 15px;

          i { color: #409EFF; margin-right: 8px; }
        }

        .section-content {
          padding: 15px;
          background: #f5f7fa;
          border-radius: 4px;
          line-height: 1.8;
          white-space: pre-wrap;
        }

        .image-gallery {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
          gap: 15px;

          .gallery-image {
            width: 100%;
            height: 200px;
            border-radius: 8px;
            cursor: pointer;
          }
        }
      }
    }
  }

  @media screen and (max-width: 768px) {
    .survey-detail-container { padding: 10px; }
    .detail-card .detail-header { flex-direction: column; align-items: flex-start; }
    .detail-title { font-size: 22px !important; }
  }
  </style>
