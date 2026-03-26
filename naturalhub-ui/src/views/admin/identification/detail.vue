<template>
  <div class="app-container">
    <el-row class="mb8">
      <el-col :span="24">
        <el-button icon="el-icon-back" size="small" @click="goBack">返回列表</el-button>
      </el-col>
    </el-row>

    <div v-loading="loading">
      <!-- 基本信息 -->
      <el-card class="box-card mb12">
        <div slot="header" class="card-header">
          <span><i class="el-icon-document"></i> 基本信息</span>
          <div>
            <el-tag :type="getStatusType(identification.status)" size="medium" style="margin-right:8px">
              {{ getStatusText(identification.status) }}
            </el-tag>
            <el-tag :type="getAuditStatusType(identification.auditStatus)" size="medium">
              {{ getAuditStatusText(identification.auditStatus) }}
            </el-tag>
          </div>
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="标题" :span="2">
            <strong>{{ identification.title || '暂无标题' }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="发布人">{{ identification.userName || identification.createBy }}</el-descriptions-item>
          <el-descriptions-item label="观察时间">
            {{ parseTime(identification.observationTime, '{y}-{m}-{d} {h}:{i}') }}
          </el-descriptions-item>
          <el-descriptions-item label="观察地点">{{ identification.location || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="经纬度" v-if="identification.latitude && identification.longitude">
            {{ identification.latitude }}, {{ identification.longitude }}
          </el-descriptions-item>
          <el-descriptions-item label="回答数量">
            <el-tag type="primary" size="small">{{ identification.answerCount || 0 }} 条</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="浏览次数">{{ identification.viewCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="分享状态">
            <el-tag :type="identification.isShared === 1 ? 'success' : 'info'" size="small">
              {{ identification.isShared === 1 ? '已分享到广场' : '未分享' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ identification.createTime || '暂无' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="identification.description" class="info-block mt12">
          <div class="block-label"><i class="el-icon-document"></i> 详细描述</div>
          <div class="block-content">{{ identification.description }}</div>
        </div>
        <div v-if="identification.features" class="info-block">
          <div class="block-label"><i class="el-icon-view"></i> 物种特征</div>
          <div class="block-content">{{ identification.features }}</div>
        </div>
        <div v-if="identification.habitat" class="info-block">
          <div class="block-label"><i class="el-icon-place"></i> 生境信息</div>
          <div class="block-content">{{ identification.habitat }}</div>
        </div>
      </el-card>

      <!-- 鉴定图片 -->
      <el-card v-if="imageList.length > 0" class="box-card mb12">
        <div slot="header" class="card-header"><span><i class="el-icon-picture"></i> 鉴定图片</span></div>
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
      </el-card>

      <!-- AI 识别结果 -->
      <el-card v-if="identification.aiSpeciesName" class="box-card mb12">
        <div slot="header" class="card-header"><span><i class="el-icon-cpu"></i> AI 识别结果</span></div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="识别物种">
            <strong style="font-size:16px;color:#303133">{{ identification.aiSpeciesName }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="置信度">
            <el-tag
              :type="identification.aiScore >= 0.8 ? 'success' : (identification.aiScore >= 0.5 ? 'warning' : 'info')"
              size="medium"
            >
              {{ (identification.aiScore * 100).toFixed(1) }}%
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="百度百科简介" :span="2" v-if="identification.baikeDescription">
            {{ identification.baikeDescription }}
          </el-descriptions-item>
          <el-descriptions-item label="百科链接" v-if="identification.baikeUrl">
            <a :href="identification.baikeUrl" target="_blank" class="link-text">
              <i class="el-icon-link"></i> 查看百度百科
            </a>
          </el-descriptions-item>
          <el-descriptions-item label="百科图片" v-if="identification.baikeImageUrl">
            <el-image
              :src="identification.baikeImageUrl"
              :preview-src-list="[identification.baikeImageUrl]"
              fit="contain"
              style="width:120px;height:80px;border-radius:4px;"
            />
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 审核信息 + 操作 -->
      <el-card class="box-card mb12">
        <div slot="header" class="card-header"><span><i class="el-icon-s-check"></i> 审核信息</span></div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="审核状态">
            <el-tag :type="getAuditStatusType(identification.auditStatus)" size="small">
              {{ getAuditStatusText(identification.auditStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核人" v-if="identification.auditBy">{{ identification.auditBy }}</el-descriptions-item>
          <el-descriptions-item label="审核时间" v-if="identification.auditTime">{{ identification.auditTime }}</el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="2" v-if="identification.auditRemark">{{ identification.auditRemark }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt12" v-if="identification.auditStatus === 1">
          <el-button type="success" size="small" icon="el-icon-check" @click="openApprove"
            v-hasPermi="['identification:admin:approve']">通过</el-button>
          <el-button type="danger" size="small" icon="el-icon-close" @click="openReject"
            v-hasPermi="['identification:admin:reject']">驳回</el-button>
        </div>
      </el-card>

      <!-- 投票信息 -->
      <el-card v-if="identification.isShared === 1" class="box-card mb12">
        <div slot="header" class="card-header">
          <span><i class="el-icon-data-analysis"></i> 投票信息</span>
          <el-button
            v-if="voteStatus.voteStatus === '1'"
            type="warning" size="mini" icon="el-icon-finished"
            v-hasPermi="['identification:admin:edit']"
            @click="handleEndVoting"
          >结束投票</el-button>
        </div>
        <el-row :gutter="20" class="vote-score-row">
          <el-col :span="12">
            <div class="vote-item">
              <span class="vote-label">同意</span>
              <span class="vote-value" style="color:#67C23A">{{ voteStatus.agreeScore }} 分</span>
              <el-progress :percentage="getAgreePercent()" status="success" :stroke-width="10" />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="vote-item">
              <span class="vote-label">不同意</span>
              <span class="vote-value" style="color:#F56C6C">{{ voteStatus.disagreeScore }} 分</span>
              <el-progress :percentage="getDisagreePercent()" status="exception" :stroke-width="10" />
            </div>
          </el-col>
        </el-row>
        <div class="vote-meta mt8">
          <el-tag :type="getVoteStatusType()" size="small" style="margin-right:8px">{{ getVoteStatusText() }}</el-tag>
          <el-tag
            v-if="voteStatus.voteResult && voteStatus.voteResult !== '2'"
            :type="voteStatus.voteResult === '0' ? 'success' : 'danger'"
            size="small"
          >{{ voteStatus.voteResult === '0' ? '已通过' : '未通过' }}</el-tag>
          <span class="vote-hint">（任一方达到20分自动结束）</span>
        </div>
        <div class="mt16">
          <div class="sub-title"><i class="el-icon-data-board"></i> 投票记录（{{ voteList.length }}）</div>
          <el-table v-loading="voteLoading" :data="voteList" stripe border size="small" class="mt8">
            <el-table-column label="投票人" prop="userName" width="120" />
            <el-table-column label="用户类型" prop="userType" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.userType === '1' ? 'success' : 'info'" size="mini">
                  {{ scope.row.userType === '1' ? '鉴定者' : '探索者' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="投票" prop="voteType" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.voteType == '0' ? 'success' : 'danger'" size="mini">
                  {{ scope.row.voteType == '0' ? '同意' : '不同意' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="权重" prop="voteWeight" width="80" align="center">
              <template slot-scope="scope">
                <el-tag type="warning" size="mini">{{ scope.row.voteWeight }}分</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="投票时间" prop="voteTime" min-width="160" />
          </el-table>
        </div>
      </el-card>

      <!-- 鉴定回答 -->
      <el-card class="box-card mb12">
        <div slot="header" class="card-header">
          <span><i class="el-icon-chat-dot-round"></i> 鉴定回答（{{ answers.length }}）</span>
        </div>
        <el-empty v-if="answers.length === 0" description="暂无鉴定回答" :image-size="80" />
        <el-table v-else :data="answers" stripe border size="small">
          <el-table-column label="回答人" prop="userName" width="120" />
          <el-table-column label="鉴定物种" prop="speciesName" width="160">
            <template slot-scope="scope">
              <strong v-if="scope.row.speciesName">{{ scope.row.speciesName }}</strong>
              <span v-else style="color:#C0C4CC">未填写</span>
            </template>
          </el-table-column>
          <el-table-column label="可信度" prop="confidence" width="90" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.confidence === 'high'" type="success" size="mini">高</el-tag>
              <el-tag v-else-if="scope.row.confidence === 'medium'" type="warning" size="mini">中</el-tag>
              <el-tag v-else-if="scope.row.confidence === 'low'" type="info" size="mini">低</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="最佳答案" prop="isBest" width="90" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.isBest === '1'" type="success" size="mini">最佳</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="回答内容" prop="content" min-width="200" show-overflow-tooltip />
          <el-table-column label="参考依据" prop="reference" min-width="160" show-overflow-tooltip />
          <el-table-column label="回答时间" prop="createTime" width="160" />
        </el-table>
      </el-card>
    </div>

    <!-- 审核通过弹窗 -->
    <el-dialog title="审核通过" :visible.sync="approveOpen" width="500px" append-to-body>
      <el-form ref="approveForm" :model="approveForm" label-width="100px">
        <el-form-item label="审核意见">
          <el-input v-model="approveForm.auditRemark" type="textarea" :rows="4"
            placeholder="请输入审核意见（可选）" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="approveOpen = false">取 消</el-button>
        <el-button type="success" @click="submitApprove">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 审核驳回弹窗 -->
    <el-dialog title="审核驳回" :visible.sync="rejectOpen" width="500px" append-to-body>
      <el-form ref="rejectForm" :model="rejectForm" :rules="rejectRules" label-width="100px">
        <el-form-item label="驳回原因" prop="auditRemark">
          <el-input v-model="rejectForm.auditRemark" type="textarea" :rows="4"
            placeholder="请输入驳回原因" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="rejectOpen = false">取 消</el-button>
        <el-button type="danger" @click="submitReject">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {
  getIdentification,
  auditIdentification,
  getVoteStatusAdmin,
  getVoteDetailsAdmin,
  endVotingAdmin
} from "@/api/admin/identification";

export default {
  name: "IdentificationAdminDetail",
  data() {
    return {
      loading: true,
      identification: {},
      answers: [],
      voteList: [],
      voteStatus: { agreeScore: 0, disagreeScore: 0, voteStatus: '0', voteResult: '2' },
      imageList: [],
      voteLoading: false,
      approveOpen: false,
      approveForm: { auditRemark: '' },
      rejectOpen: false,
      rejectForm: { auditRemark: '' },
      rejectRules: {
        auditRemark: [{ required: true, message: "驳回原因不能为空", trigger: "blur" }]
      }
    };
  },
  created() {
    this.getDetail();
  },
  methods: {
    getDetail() {
      const id = this.$route.params.id || this.$route.query.id;
      this.loading = true;
      getIdentification(id).then(response => {
        this.identification = response.data;
        this.answers = response.answers || [];
        if (this.identification.images) {
          this.imageList = this.identification.images.split(",");
        }
        this.loading = false;
        if (this.identification.isShared === 1) {
          this.getVoteStatusInfo();
          this.getVoteDetails();
        }
      }).catch(() => { this.loading = false; });
    },
    getVoteStatusInfo() {
      const id = this.$route.params.id || this.$route.query.id;
      getVoteStatusAdmin(id).then(response => {
        this.voteStatus = response.data || { agreeScore: 0, disagreeScore: 0, voteStatus: '0', voteResult: '2' };
      });
    },
    getVoteDetails() {
      const id = this.$route.params.id || this.$route.query.id;
      this.voteLoading = true;
      getVoteDetailsAdmin(id).then(response => {
        this.voteList = response.data || [];
        this.voteLoading = false;
      }).catch(() => { this.voteLoading = false; });
    },
    goBack() { this.$router.go(-1); },
    openApprove() {
      this.approveForm = { identificationId: this.identification.identificationId, auditStatus: 2, auditRemark: '' };
      this.approveOpen = true;
    },
    submitApprove() {
      auditIdentification(this.approveForm).then(() => {
        this.$modal.msgSuccess("审核通过");
        this.approveOpen = false;
        this.getDetail();
      });
    },
    openReject() {
      this.rejectForm = { identificationId: this.identification.identificationId, auditStatus: 3, auditRemark: '' };
      this.rejectOpen = true;
    },
    submitReject() {
      this.$refs.rejectForm.validate(valid => {
        if (valid) {
          auditIdentification(this.rejectForm).then(() => {
            this.$modal.msgSuccess("审核驳回");
            this.rejectOpen = false;
            this.getDetail();
          });
        }
      });
    },
    handleEndVoting() {
      const id = this.$route.params.id || this.$route.query.id;
      this.$modal.confirm("确认结束投票吗？").then(() => endVotingAdmin(id)).then(() => {
        this.$modal.msgSuccess("投票已结束");
        this.getVoteStatusInfo();
      }).catch(() => {});
    },
    getStatusText(v) { return { 0: '未鉴定', 1: '鉴定中', 2: '已鉴定' }[v] || v; },
    getStatusType(v) { return { 0: 'info', 1: 'primary', 2: 'success' }[v] || 'info'; },
    getAuditStatusText(v) { return { 0: '草稿', 1: '待审核', 2: '已通过', 3: '已驳回' }[v] || v; },
    getAuditStatusType(v) { return { 0: '', 1: 'warning', 2: 'success', 3: 'danger' }[v] || ''; },
    getVoteStatusType() { return { '0': 'info', '1': 'primary', '2': 'success' }[this.voteStatus.voteStatus] || 'info'; },
    getVoteStatusText() { return { '0': '未开始', '1': '进行中', '2': '已结束' }[this.voteStatus.voteStatus] || '未知'; },
    getAgreePercent() {
      const t = this.voteStatus.agreeScore + this.voteStatus.disagreeScore;
      return t === 0 ? 0 : Math.round((this.voteStatus.agreeScore / t) * 100);
    },
    getDisagreePercent() {
      const t = this.voteStatus.agreeScore + this.voteStatus.disagreeScore;
      return t === 0 ? 0 : Math.round((this.voteStatus.disagreeScore / t) * 100);
    }
  }
};
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.mb12 { margin-bottom: 12px; }
.mt8 { margin-top: 8px; }
.mt12 { margin-top: 12px; }
.mt16 { margin-top: 16px; }
.info-block { margin-top: 12px; }
.block-label { font-weight: 600; color: #606266; margin-bottom: 6px; font-size: 13px; }
.block-content { background: #f5f7fa; padding: 10px 14px; border-radius: 4px; line-height: 1.8; color: #303133; font-size: 13px; }
.image-gallery { display: flex; flex-wrap: wrap; gap: 12px; }
.gallery-image { width: 160px; height: 120px; border-radius: 6px; cursor: pointer; }
.vote-score-row { margin: 12px 0; }
.vote-item { padding: 8px 0; }
.vote-label { font-weight: 600; color: #606266; margin-right: 10px; }
.vote-value { font-size: 18px; font-weight: bold; margin-right: 12px; }
.vote-meta { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
.vote-hint { color: #909399; font-size: 12px; }
.sub-title { font-weight: 600; color: #303133; font-size: 14px; }
.link-text { color: #409EFF; text-decoration: none; }
.link-text:hover { text-decoration: underline; }
</style>
