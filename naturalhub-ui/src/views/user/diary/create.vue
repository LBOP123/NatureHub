<template>
  <div class="diary-create-container">
    <el-card class="create-card">
      <div slot="header" class="card-header">
        <i class="el-icon-edit"></i>
        <span>{{ form.diaryId ? '编辑观察日志' : '新建观察日志' }}</span>
      </div>

      <el-form ref="diaryForm" :model="form" :rules="rules" label-width="100px" class="diary-form">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="日志标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入日志标题" maxlength="200" show-word-limit/>
        </el-form-item>
        <el-form-item label="观察日期" prop="observationDate">
          <el-date-picker v-model="form.observationDate" type="datetime" placeholder="选择观察日期"
                          value-format="yyyy-MM-dd HH:mm:ss" :picker-options="pickerOptions" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="观察地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入观察地点" maxlength="200" show-word-limit/>
        </el-form-item>

        <el-divider content-position="left">关联观察记录</el-divider>
        <el-form-item label="关联记录">
          <el-button type="primary" icon="el-icon-plus" @click="toggleRecordList">
            {{ showRecordList ? '隐藏观察记录' : '添加观察记录' }}
          </el-button>

          <div v-if="showRecordList" class="record-list-container">
            <div class="record-list-toolbar">
              <el-input v-model="searchText" placeholder="搜索物种名称..." prefix-icon="el-icon-search" clearable
                        style="width: 200px"/>
            </div>
            <el-table
              :data="allRecords"
              v-loading="recordsLoading"
              style="width: 100%; margin-top: 15px"
              max-height="400"
              @selection-change="handleSelectionChange"
            >
              <el-table-column type="selection" width="50"/>
              <el-table-column label="缩略图" width="80">
                <template slot-scope="scope">
                  <el-image v-if="scope.row.images" :src="getFirstImage(scope.row.images)"
                            style="width: 60px; height: 60px" fit="cover"/>
                  <span v-else style="color: #999">无图片</span>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" min-width="120"/>
              <el-table-column prop="speciesName" label="物种名称" min-width="100"/>
              <el-table-column prop="location" label="地点" min-width="120"/>
              <el-table-column prop="observationTime" label="观察时间" min-width="160" sortable @sort-change="handleTimeSort">
                <template slot-scope="scope">
                  {{ scope.row.observationTime ? scope.row.observationTime.substring(0, 16) : '-' }}
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div v-if="form.recordIds && form.recordIds.length > 0" class="selected-records-section">
            <h4 class="section-subtitle">已选择的观察记录 ({{ form.recordIds.length }})</h4>
            <div class="selected-records-list">
              <div v-for="(record, index) in relatedRecords" :key="record.recordId" class="record-item">
                <div class="record-index">{{ index + 1 }}</div>
                <div class="record-info">
                  <div class="record-species">{{ record.speciesName }}</div>
                  <div class="record-title">{{ record.title }}</div>
                </div>
                <el-button type="text" icon="el-icon-delete" @click="removeRecord(record.recordId)"/>
              </div>
            </div>
            <div class="species-route">
              <h4 class="section-subtitle">物种观察路线</h4>
              <div class="route-content">{{ generatedRoute }}</div>
              <el-button type="text" icon="el-icon-document-copy" @click="copyRoute">复制路线</el-button>
            </div>
          </div>

          <div v-else class="empty-tip">
            <i class="el-icon-info"></i> 暂未关联任何观察记录
          </div>
        </el-form-item>

        <el-divider content-position="left">环境信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="天气情况">
              <el-input v-model="form.weather" placeholder="如：晴朗、多云、小雨"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="温度">
              <el-input v-model="form.temperature" placeholder="如：15-20℃"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="路线信息">
          <el-input v-model="form.routeInfo" type="textarea" :rows="3"
                    placeholder="描述你的探索路线，如：从公园东门进入，沿湖边小路前行..." maxlength="500" show-word-limit/>
        </el-form-item>

        <el-divider content-position="left">观察内容</el-divider>
        <el-form-item label="发现物种">
          <el-input v-model="form.speciesFound" type="textarea" :rows="4"
                    placeholder="记录发现的物种，如：白头鹎 3只、喜鹊 2只、迎春花 多处" maxlength="1000" show-word-limit/>
        </el-form-item>
        <el-form-item label="日志内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="记录你的观察心得、发现和感悟..."
                    maxlength="5000" show-word-limit/>
        </el-form-item>

        <el-divider content-position="left">其他信息</el-divider>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，如：春季,鸟类,植物" maxlength="200"/>
          <div class="form-tip"><i class="el-icon-info"></i> 添加标签可以帮助你更好地分类和查找日志</div>
        </el-form-item>
        <el-form-item label="可见性" prop="visibility">
          <el-radio-group v-model="form.visibility">
            <el-radio label="0"><i class="el-icon-lock"></i> 私密（仅自己可见）</el-radio>
            <el-radio label="1"><i class="el-icon-view"></i> 公开（所有人可见）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他补充信息"/>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitLoading"><i class="el-icon-check"></i> 保存日志
          </el-button>
          <el-button @click="resetForm"><i class="el-icon-refresh-left"></i> 重置</el-button>
          <el-button @click="goBack"><i class="el-icon-back"></i> 返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import {getDiary, addDiary, updateDiary} from "@/api/user/diary";
import {listRecord} from "@/api/user/record";
import {getDicts} from "@/api/system/dict/data";

export default {
  name: "DiaryCreate",
  data() {
    return {
      form: {
        diaryId: null,
        title: null,
        observationDate: null,
        location: null,
        routeInfo: null,
        weather: null,
        temperature: null,
        speciesFound: null,
        content: null,
        visibility: '0',
        tags: null,
        remark: null,
        recordIds: []
      },
      relatedRecords: [],
      allRecords: [],
      showRecordList: false,
      recordsLoading: false,
      searchText: '',
      filterType: null,
      speciesTypeOptions: [],
      rules: {
        title: [{required: true, message: "日志标题不能为空", trigger: "blur"}, {
          min: 2,
          max: 200,
          message: "标题长度在2到200个字符",
          trigger: "blur"
        }],
        observationDate: [{required: true, message: "请选择观察日期", trigger: "change"}],
        location: [{required: true, message: "请输入观察地点", trigger: "blur"}],
        content: [{required: true, message: "请输入日志内容", trigger: "blur"}, {
          min: 10,
          max: 5000,
          message: "内容长度在10到5000个字符",
          trigger: "blur"
        }],
        visibility: [{required: true, message: "请选择可见性", trigger: "change"}]
      },
      submitLoading: false,
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      }
    };
  },
  computed: {
    filteredRecords() {
      if (!this.allRecords || this.allRecords.length === 0) {
        console.log('allRecords 为空');
        return [];
      }

      return this.allRecords.filter(record => {
        // 搜索过滤
        if (this.searchText) {
          const searchLower = this.searchText.toLowerCase();
          const matchSearch = (record.speciesName && record.speciesName.toLowerCase().includes(searchLower)) ||
            (record.title && record.title.toLowerCase().includes(searchLower));
          if (!matchSearch) return false;
        }

        // 类型过滤
        if (this.filterType !== null && this.filterType !== undefined) {
          if (record.speciesType != this.filterType) return false;
        }

        return true;
      });
    },
    generatedRoute() {
      if (!this.relatedRecords || this.relatedRecords.length === 0) return '暂无物种';
      return this.relatedRecords.map((r, i) => `${i + 1}. ${r.speciesName}`).join(' → ');
    }
  },
  created() {
    this.loadDicts();
    const diaryId = this.$route.query.id;
    if (diaryId) this.loadDiaryData(diaryId);
  },
  methods: {
    getFirstImage(images) {
      if (!images) return '';
      try {
        const imageArray = typeof images === 'string' ? JSON.parse(images) : images;
        return imageArray && imageArray.length > 0 ? imageArray[0] : '';
      } catch (e) {
        return '';
      }
    },
    handleTimeSort(column) {
      if (column.order === 'ascending') {
        this.allRecords.sort((a, b) => new Date(a.observationTime) - new Date(b.observationTime));
      } else if (column.order === 'descending') {
        this.allRecords.sort((a, b) => new Date(b.observationTime) - new Date(a.observationTime));
      }
    },
    getTableData() {
      if (!this.allRecords || this.allRecords.length === 0) {
        return [];
      }

      // 直接返回所有记录，不过滤
      return this.allRecords;
    },
    loadDicts() {
      getDicts('nh_species_type').then(res => {
        this.speciesTypeOptions = res.data || [];
      }).catch(() => {
        this.speciesTypeOptions = [];
      });
    },
    loadDiaryData(diaryId) {
      getDiary(diaryId).then(response => {
        this.form = response.data;
        if (this.form.relatedRecords) {
          this.relatedRecords = this.form.relatedRecords;
          this.form.recordIds = this.form.relatedRecords.map(r => r.recordId);
        }
      });
    },
    toggleRecordList() {
      this.showRecordList = !this.showRecordList;
      if (this.showRecordList) {
        this.loadAllRecords();
      }
    },
    loadAllRecords() {
      this.recordsLoading = true;
      listRecord({pageNum: 1, pageSize: 1000}).then(res => {
        // 这里改了！！！
        this.allRecords = res.data || res.rows || res.list || [];
        console.log('加载的记录数:', this.allRecords.length);
      }).catch(err => {
        console.error('加载失败:', err);
        this.$message.error('加载观察记录失败');
        this.allRecords = [];
      }).finally(() => {
        this.recordsLoading = false;
      });
    },
    handleSelectionChange(selection) {
      this.form.recordIds = selection.map(r => r.recordId);
      this.loadRelatedRecords();
      // 自动填充路线信息
      this.updateRouteInfo();
    },
    loadRelatedRecords() {
      if (!this.form.recordIds || this.form.recordIds.length === 0) {
        this.relatedRecords = [];
        this.form.routeInfo = '';  // 清空路线
        return;
      }

      // 按选择顺序排序
      this.relatedRecords = this.allRecords
        .filter(r => this.form.recordIds.includes(r.recordId))
        .sort((a, b) => this.form.recordIds.indexOf(a.recordId) - this.form.recordIds.indexOf(b.recordId));

      // 更新路线信息
      this.updateRouteInfo();
    },
    removeRecord(recordId) {
      this.form.recordIds = this.form.recordIds.filter(id => id !== recordId);
      this.loadRelatedRecords();
      // 移除后自动更新路线
      this.updateRouteInfo();
    },
    copyRoute() {
      navigator.clipboard.writeText(this.generatedRoute).then(() => {
        this.$message.success('路线已复制到剪贴板');
      }).catch(() => {
        this.$message.error('复制失败');
      });
    },
    submitForm() {
      this.$refs["diaryForm"].validate(valid => {
        if (valid) {
          this.submitLoading = true;
          const request = this.form.diaryId ? updateDiary(this.form) : addDiary(this.form);
          request.then(response => {
            this.$message.success(this.form.diaryId ? '修改成功' : '保存成功');
            this.$router.push('/user/diary/list');
          }).catch(() => {
            this.submitLoading = false;
          }).finally(() => {
            this.submitLoading = false;
          });
        }
      });
    },
    resetForm() {
      this.$refs["diaryForm"].resetFields();
      this.relatedRecords = [];
      this.showRecordList = false;
    },
    goBack() {
      this.$router.go(-1);
    },
    updateRouteInfo() {
      if (this.relatedRecords && this.relatedRecords.length > 0) {
        // 根据选择顺序生成路线
        const route = this.relatedRecords
          .map((r, i) => `${i + 1}. ${r.speciesName}`)
          .join(' → ');
        // 自动填充到路线信息字段
        this.form.routeInfo = route;
      }
    },
    parseTime(time, format) {
      return this.$options.filters.parseTime(time, format);
    }
  }
};
</script>

<style lang="scss" scoped>
.diary-create-container {
  padding: 20px;

  .create-card {
    max-width: 1200px;
    margin: 0 auto;

    .card-header {
      font-size: 18px;
      font-weight: bold;

      i {
        margin-right: 8px;
        color: #67C23A;
      }
    }
  }

  .diary-form {
    .el-divider {
      margin: 30px 0 20px;

      ::v-deep .el-divider__text {
        font-weight: bold;
        color: #409EFF;
      }
    }

    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 5px;

      i {
        margin-right: 5px;
      }
    }

    .record-list-container {
      margin-top: 15px;
      padding: 15px;
      background: #f5f7fa;
      border-radius: 4px;

      .record-list-toolbar {
        display: flex;
        gap: 10px;
        margin-bottom: 15px;
        flex-wrap: wrap;
      }
    }

    .selected-records-section {
      margin-top: 20px;
      padding: 15px;
      background: #f0f9ff;
      border-left: 4px solid #409EFF;
      border-radius: 4px;

      .section-subtitle {
        font-size: 14px;
        font-weight: bold;
        color: #303133;
        margin: 0 0 10px 0;
      }

      .selected-records-list {
        margin-bottom: 20px;

        .record-item {
          display: flex;
          align-items: center;
          padding: 10px;
          background: #fff;
          border-radius: 4px;
          margin-bottom: 8px;
          border: 1px solid #dcdfe6;

          .record-index {
            width: 30px;
            height: 30px;
            background: #409EFF;
            color: #fff;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            margin-right: 10px;
            flex-shrink: 0;
          }

          .record-info {
            flex: 1;

            .record-species {
              font-weight: bold;
              color: #303133;
              font-size: 14px;
            }

            .record-title {
              color: #909399;
              font-size: 12px;
              margin-top: 3px;
            }
          }
        }
      }

      .species-route {
        padding: 15px;
        background: #fff;
        border-radius: 4px;
        border: 1px solid #dcdfe6;

        .route-content {
          padding: 10px;
          background: #f5f7fa;
          border-radius: 4px;
          font-size: 14px;
          color: #606266;
          line-height: 1.6;
          word-break: break-all;
          margin: 10px 0;
          font-weight: 500;
        }
      }
    }

    .empty-tip {
      color: #909399;
      font-size: 14px;
      margin-top: 10px;

      i {
        margin-right: 5px;
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .diary-create-container {
    padding: 10px;

    .diary-form {
      ::v-deep .el-form-item__label {
        width: 80px !important;
      }

      ::v-deep .el-form-item__content {
        margin-left: 80px !important;
      }
    }
  }
}
</style>
