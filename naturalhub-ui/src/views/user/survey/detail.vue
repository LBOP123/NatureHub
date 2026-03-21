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
          >编辑
          </el-button>
          <el-button
            v-if="survey.auditStatus === 0"
            type="success"
            icon="el-icon-s-promotion"
            size="small"
            @click="handleSubmitReview"
          >提交审核
          </el-button>
          <el-button
            v-if="survey.auditStatus === 2 && survey.isShared === 0"
            type="success"
            icon="el-icon-share"
            size="small"
            @click="handleShare"
          >分享到社群
          </el-button>
          <el-button
            type="danger"
            icon="el-icon-delete"
            size="small"
            @click="handleDelete"
          >删除
          </el-button>
        </div>
      </div>

      <!-- 标题和状态 -->
      <div class="detail-title-section">
        <h1 class="detail-title">{{ survey.title || '暂无标题' }}</h1>
        <div class="detail-status">
          <el-tag
            :type="getAuditStatusType(survey.auditStatus)"
            size="medium"
          >
            {{ getAuditStatusText(survey.auditStatus) }}
          </el-tag>
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
          <el-tag type="success" size="small">{{ getHabitatTypeText(survey.habitatType) || '暂无' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="调查方法">
          {{ getSurveyTypeText(survey.surveyMethod) || '暂无' }}
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
          {{ survey.temperature ? survey.temperature + '℃' : '暂无' }}
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

    <!-- 分享弹窗 -->
    <el-dialog title="分享到社群" :visible.sync="shareDialogVisible" width="600px" append-to-body>
      <el-form :model="shareForm" :rules="shareRules" ref="shareForm" label-width="100px">
        <el-form-item label="分享板块">
          <el-input value="野外调查" disabled />
          <div style="color:#909399;font-size:12px;margin-top:4px">野外调查只能分享到「野外调查」板块</div>
        </el-form-item>
        <el-form-item label="分享标题" prop="title">
          <el-input v-model="shareForm.title" placeholder="请输入分享标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分享内容" prop="content">
          <el-input v-model="shareForm.content" type="textarea" :rows="5"
            placeholder="请输入分享内容（将保存到话题描述）" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="shareDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShare" :loading="shareLoading">确认分享</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {getSurvey, delSurvey, shareSurvey, updateSurvey} from "@/api/user/survey";

export default {
  name: "SurveyDetail",
  data() {
    return {
      loading: true,
      auditStatusOptions: [],
      surveyMethodOptions: [],
      habitatTypeOptions: [],
      survey: {},
      imageList: [],

      mapDialogVisible: false,
      shareDialogVisible: false,
      shareLoading: false,
      shareForm: {
        title: '',
        content: ''
      },
      shareRules: {
        title: [{ required: true, message: '请输入分享标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入分享内容', trigger: 'blur' }]
      }
    };
  },
  computed: {
    isOwner() {
      return this.survey.userId === this.$store.state.user.userId;
    }
  },
  created() {
    this.getDicts('nh_audit_status').then(res => {
      this.auditStatusOptions = res.data || []
    })
    this.getDicts('nh_habitat_type').then(res => {
      this.habitatTypeOptions = res.data
    })
    this.getDicts('nh_survey_method').then(res => {
      this.surveyMethodOptions = res.data
    })
    this.getDetail();
  },
  methods: {
    getDetail() {
      const surveyId = this.$route.query.id;
      getSurvey(surveyId).then(response => {
        this.survey = response.data;

        if (this.survey.images) {
          // images 是逗号分隔的字符串，直接 split
          this.imageList = this.survey.images.split(',').filter(url => url.trim());
        }

        this.loading = false;
      });
    },
    goBack() {
      this.$router.go(-1);
    },
    /** 提交审核 */
    handleSubmitReview() {
      this.$confirm('提交审核后将无法修改，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return updateSurvey({surveyId: this.survey.surveyId, auditStatus: 1})
      }).then(() => {
        this.$message.success('提交审核成功')
        this.getDetail()
      })
    },
    handleEdit() {
      this.$router.push({
        path: "/user/survey/create",
        query: {id: this.survey.surveyId}
      });
    },
    handleShare() {
      this.shareForm = {
        title: this.survey.title || '野外调查分享',
        content: this.survey.description || ''
      };
      this.shareDialogVisible = true;
    },
    confirmShare() {
      this.$refs.shareForm.validate(valid => {
        if (valid) {
          this.shareLoading = true;
          shareSurvey(this.survey.surveyId, {
            title: this.shareForm.title,
            content: this.shareForm.content
          }).then(() => {
            this.$message.success('分享成功');
            this.survey.isShared = 1;
            this.shareDialogVisible = false;
            this.$router.push('/user/community');
          }).catch(error => {
            this.$message.error(error.msg || '分享失败');
          }).finally(() => {
            this.shareLoading = false;
          });
        }
      });
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
    getSurveyTypeText(val) {
      const d = (this.surveyMethodOptions || []).find(d => parseInt(d.dictValue) === val)
      return d ? d.dictLabel : (val || '-')
    },
    getHabitatTypeText(val) {
      const d = (this.habitatTypeOptions || []).find(d => parseInt(d.dictValue) === val)
      return d ? d.dictLabel : (val || '-')
    },
    getAuditStatusText(status) {
      const item = this.auditStatusOptions.find(d => d.dictValue == status);
      return item ? item.dictLabel : '';
    },
    getAuditStatusType(status) {
      switch (status) {
        case 0:
          return '';
        case 1:
          return 'warning';
        case 2:
          return 'success';
        case 3:
          return 'danger';
        default:
          return '';
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

        i {
          color: #409EFF;
          margin-right: 8px;
        }
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
  .survey-detail-container {
    padding: 10px;
  }
  .detail-card .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .detail-title {
    font-size: 22px !important;
  }
}
</style>
