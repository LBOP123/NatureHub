<template>
  <div class="diary-container">
    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新建日志</el-button>
        <el-button icon="el-icon-download" @click="handleExport">导出Excel</el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="queryParams.title"
          placeholder="搜索日志标题"
          clearable
          style="width: 200px; margin-right: 10px;"
          @keyup.enter.native="handleQuery"
        />
        <el-button icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </div>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <el-radio-group v-model="queryParams.isArchived" @change="handleQuery">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="0">活跃</el-radio-button>
        <el-radio-button label="1">已归档</el-radio-button>
      </el-radio-group>
      <el-radio-group v-model="queryParams.visibility" @change="handleQuery" style="margin-left: 20px;">
        <el-radio-button label="">全部可见性</el-radio-button>
        <el-radio-button label="0">私密</el-radio-button>
        <el-radio-button label="1">公开</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 日志列表 -->
    <div class="diary-list">
      <el-card
        v-for="diary in diaryList"
        :key="diary.diaryId"
        class="diary-card"
        shadow="hover"
      >
        <div class="diary-header">
          <div class="diary-title-row">
            <h3 class="diary-title" @click="handleView(diary)">{{ diary.title }}</h3>
            <div class="diary-badges">
              <el-tag v-if="diary.visibility === '0'" type="info" size="small">私密</el-tag>
              <el-tag v-else type="success" size="small">公开</el-tag>
              <el-tag v-if="diary.isArchived === '1'" type="warning" size="small">已归档</el-tag>
            </div>
          </div>
          <div class="diary-meta">
            <span><i class="el-icon-time"></i> {{ parseTime(diary.observationDate, '{y}-{m}-{d}') }}</span>
            <span v-if="diary.location"><i class="el-icon-location"></i> {{ diary.location }}</span>
            <span v-if="diary.weather"><i class="el-icon-sunny"></i> {{ diary.weather }}</span>
          </div>
        </div>

        <div class="diary-content">
          <p>{{ truncate(diary.content, 150) }}</p>
        </div>

        <div class="diary-tags" v-if="diary.tags">
          <el-tag
            v-for="tag in diary.tags.split(',')"
            :key="tag"
            size="mini"
            type="info"
          >
            {{ tag }}
          </el-tag>
        </div>

        <div class="diary-actions">
          <el-button type="text" icon="el-icon-view" @click="handleView(diary)">查看</el-button>
          <el-button
            v-if="diary.isArchived !== '1'"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(diary)"
          >
            编辑
          </el-button>
          <el-button
            type="text"
            :icon="diary.isArchived === '1' ? 'el-icon-folder-opened' : 'el-icon-folder'"
            @click="handleArchive(diary)"
          >
            {{ diary.isArchived === '1' ? '取消归档' : '归档' }}
          </el-button>
          <el-button type="text" icon="el-icon-delete" @click="handleDelete(diary)" style="color: #f56c6c;">删除
          </el-button>
        </div>
      </el-card>

      <el-empty v-if="diaryList.length === 0" description="暂无日志记录，快去创建第一篇吧！"></el-empty>
    </div>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="日志标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入日志标题" maxlength="200"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="观察日期" prop="observationDate">
              <el-date-picker
                v-model="form.observationDate"
                type="datetime"
                placeholder="选择观察日期"
                style="width: 100%;"
                value-format="yyyy-MM-dd HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="观察地点" prop="location">
              <el-input v-model="form.location" placeholder="请输入观察地点"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="天气情况">
              <el-input v-model="form.weather" placeholder="如：晴朗、多云"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="温度">
              <el-input v-model="form.temperature" placeholder="如：15-20℃"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可见性" prop="visibility">
              <el-radio-group v-model="form.visibility">
                <el-radio label="0">私密</el-radio>
                <el-radio label="1">公开</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="路线信息">
          <el-input
            v-model="form.routeInfo"
            type="textarea"
            :rows="3"
            placeholder="描述你的探索路线"
          />
        </el-form-item>

        <el-form-item label="发现物种">
          <el-input
            v-model="form.speciesFound"
            type="textarea"
            :rows="3"
            placeholder='如：白头鹎 3只、喜鹊 2只、迎春花 多处'
          />
        </el-form-item>

        <el-form-item label="日志内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="记录你的观察心得、发现和感悟..."
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-input
            v-model="form.tags"
            placeholder="多个标签用逗号分隔，如：春季,鸟类,植物"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="其他备注信息"/>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      title="日志详情"
      :visible.sync="detailVisible"
      width="800px"
    >
      <div class="diary-detail" v-if="currentDiary">
        <div class="detail-header">
          <h2>{{ currentDiary.title }}</h2>
          <div class="detail-badges">
            <el-tag v-if="currentDiary.visibility === '0'" type="info">私密</el-tag>
            <el-tag v-else type="success">公开</el-tag>
            <el-tag v-if="currentDiary.isArchived === '1'" type="warning">已归档</el-tag>
          </div>
        </div>

        <div class="detail-meta">
          <div class="meta-item">
            <span class="meta-label">观察日期：</span>
            <span>{{ parseTime(currentDiary.observationDate, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
          <div class="meta-item" v-if="currentDiary.location">
            <span class="meta-label">观察地点：</span>
            <span>{{ currentDiary.location }}</span>
          </div>
          <div class="meta-item" v-if="currentDiary.weather">
            <span class="meta-label">天气情况：</span>
            <span>{{ currentDiary.weather }}</span>
          </div>
          <div class="meta-item" v-if="currentDiary.temperature">
            <span class="meta-label">温度：</span>
            <span>{{ currentDiary.temperature }}</span>
          </div>
        </div>

        <div class="detail-section" v-if="currentDiary.routeInfo">
          <h4>路线信息</h4>
          <p>{{ currentDiary.routeInfo }}</p>
        </div>

        <div class="detail-section" v-if="currentDiary.speciesFound">
          <h4>发现物种</h4>
          <p>{{ currentDiary.speciesFound }}</p>
        </div>

        <div class="detail-section">
          <h4>日志内容</h4>
          <p class="content-text">{{ currentDiary.content }}</p>
        </div>

        <div class="detail-section" v-if="currentDiary.tags">
          <h4>标签</h4>
          <el-tag
            v-for="tag in currentDiary.tags.split(',')"
            :key="tag"
            style="margin-right: 8px;"
          >
            {{ tag }}
          </el-tag>
        </div>

        <div class="detail-footer">
          <span>创建时间：{{ parseTime(currentDiary.createTime) }}</span>
          <span v-if="currentDiary.updateTime">更新时间：{{ parseTime(currentDiary.updateTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {listDiary, getDiary, addDiary, updateDiary, delDiary, archiveDiary} from '@/api/user/diary'

export default {
  name: 'UserDiary',
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        visibility: '',
        isArchived: ''
      },
      diaryList: [],
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      detailVisible: false,
      form: {},
      currentDiary: null,
      rules: {
        title: [
          {required: true, message: '日志标题不能为空', trigger: 'blur'}
        ],
        observationDate: [
          {required: true, message: '观察日期不能为空', trigger: 'change'}
        ],
        content: [
          {required: true, message: '日志内容不能为空', trigger: 'blur'}
        ],
        visibility: [
          {required: true, message: '请选择可见性', trigger: 'change'}
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    truncate(value, length) {
      if (!value) return ''
      if (value.length <= length) return value
      return value.substring(0, length) + '...'
    },
    getList() {
      listDiary(this.queryParams).then(response => {
        this.diaryList = response.rows
        this.total = response.total
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        title: null,
        visibility: '',
        isArchived: ''
      }
      this.getList()
    },
    handleAdd() {
      this.reset()
      this.dialogVisible = true
      this.dialogTitle = '新建观察日志'
    },
    handleUpdate(row) {
      if (row.isArchived === '1') {
        this.$modal.msgWarning('该日志已归档，无法编辑！')
        return
      }
      this.reset()
      getDiary(row.diaryId).then(response => {
        this.form = response.data
        this.dialogVisible = true
        this.dialogTitle = '编辑观察日志'
      })
    },
    handleView(row) {
      getDiary(row.diaryId).then(response => {
        this.currentDiary = response.data
        this.detailVisible = true
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.diaryId != null) {
            updateDiary(this.form).then(response => {
              this.$modal.msgSuccess('修改成功')
              this.dialogVisible = false
              this.getList()
            })
          } else {
            addDiary(this.form).then(response => {
              this.$modal.msgSuccess('新增成功')
              this.dialogVisible = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除该日志？').then(() => {
        return delDiary(row.diaryId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {
      })
    },
    handleArchive(row) {
      const isArchived = row.isArchived === '1' ? '0' : '1'
      const text = isArchived === '1' ? '归档' : '取消归档'
      this.$modal.confirm('是否确认' + text + '该日志？').then(() => {
        return archiveDiary(row.diaryId, isArchived)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess(text + '成功')
      }).catch(() => {
      })
    },
    handleExport() {
      this.download('user/diary/export', {
        ...this.queryParams
      }, `观察日志_${new Date().getTime()}.xlsx`)
    },
    reset() {
      this.form = {
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
        remark: null
      }
      this.resetForm('form')
    }
  }
}
</script>

<style lang="scss" scoped>
.diary-container {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .toolbar-left {
    display: flex;
    gap: 10px;
  }

  .toolbar-right {
    display: flex;
    align-items: center;
  }
}

.filter-tabs {
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
}

.diary-list {
  display: grid;
  gap: 20px;
}

.diary-card {
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
  }

  .diary-header {
    margin-bottom: 15px;

    .diary-title-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 10px;
    }

    .diary-title {
      margin: 0;
      font-size: 20px;
      color: #2c3e50;
      cursor: pointer;
      flex: 1;

      &:hover {
        color: #43cea2;
      }
    }

    .diary-badges {
      display: flex;
      gap: 8px;
    }

    .diary-meta {
      display: flex;
      gap: 20px;
      color: #7f8c8d;
      font-size: 14px;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .diary-content {
    margin-bottom: 15px;

    p {
      color: #606266;
      line-height: 1.6;
      margin: 0;
    }
  }

  .diary-tags {
    margin-bottom: 15px;

    .el-tag {
      margin-right: 8px;
    }
  }

  .diary-actions {
    display: flex;
    gap: 10px;
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
  }
}

.diary-detail {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 2px solid #f0f0f0;

    h2 {
      margin: 0;
      color: #2c3e50;
    }

    .detail-badges {
      display: flex;
      gap: 8px;
    }
  }

  .detail-meta {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-bottom: 20px;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 8px;

    .meta-item {
      display: flex;

      .meta-label {
        font-weight: 600;
        color: #606266;
        margin-right: 8px;
      }
    }
  }

  .detail-section {
    margin-bottom: 20px;

    h4 {
      color: #2c3e50;
      margin: 0 0 10px 0;
      font-size: 16px;
    }

    p {
      color: #606266;
      line-height: 1.8;
      margin: 0;
    }

    .content-text {
      white-space: pre-wrap;
    }
  }

  .detail-footer {
    display: flex;
    justify-content: space-between;
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
    color: #909399;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    gap: 10px;

    .toolbar-left,
    .toolbar-right {
      width: 100%;
      justify-content: space-between;
    }
  }

  .filter-tabs {
    ::v-deep .el-radio-group {
      display: flex;
      flex-direction: column;
      gap: 10px;

      &:last-child {
        margin-left: 0 !important;
        margin-top: 10px;
      }
    }
  }

  .detail-meta {
    grid-template-columns: 1fr !important;
  }
}
</style>
