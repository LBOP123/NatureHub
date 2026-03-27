<template>
  <div class="app-container secrets-page">

    <!-- 顶部标题区 -->
    <div class="page-header">
      <div class="page-header__left">
        <i class="el-icon-lock header-icon"></i>
        <div>
          <h2 class="page-title">密钥管理中心</h2>
          <p class="page-subtitle">管理所有第三方服务的 API 密钥与敏感配置，修改后需重启服务生效</p>
        </div>
      </div>
      <div class="page-header__right">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增密钥</el-button>
        <el-button icon="el-icon-refresh" @click="handleRefreshCache">刷新缓存</el-button>
      </div>
    </div>

    <!-- 服务分组卡片 -->
    <div v-for="group in groupedSecrets" :key="group.name" class="secret-group">
      <div class="group-header">
        <span class="group-dot" :style="{ background: group.color }"></span>
        <span class="group-name">{{ group.label }}</span>
        <el-tag size="mini" :color="group.color" style="color:#fff;border:none;margin-left:8px">
          {{ group.items.length }} 项
        </el-tag>
      </div>

      <el-table :data="group.items" border fit class="secret-table">
        <el-table-column label="配置名称" prop="configName" min-width="160" />
        <el-table-column label="配置键名" prop="configKey" min-width="200">
          <template slot-scope="scope">
            <code class="config-key">{{ scope.row.configKey }}</code>
          </template>
        </el-table-column>
        <el-table-column label="配置值" min-width="260">
          <template slot-scope="scope">
            <div class="secret-value-cell">
              <span class="masked" v-if="!scope.row._visible">
                {{ maskValue(scope.row.configValue) }}
              </span>
              <span class="revealed" v-else>{{ scope.row.configValue }}</span>
              <el-button
                type="text"
                :icon="scope.row._visible ? 'el-icon-view' : 'el-icon-view'"
                class="toggle-btn"
                @click="toggleVisible(scope.row)"
              >{{ scope.row._visible ? '隐藏' : '显示' }}</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="160" show-overflow-tooltip />
        <el-table-column label="更新时间" prop="updateTime" min-width="155" align="center">
          <template slot-scope="scope">{{ parseTime(scope.row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="140">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 无数据提示 -->
    <el-empty v-if="!loading && allSecrets.length === 0" description="暂无密钥配置，点击「新增密钥」添加" />

    <!-- 新增 / 编辑对话框 -->
    <el-dialog :title="dialog.title" :visible.sync="dialog.open" width="560px" append-to-body>
      <el-form ref="secretForm" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="如：百度AI API Key" />
        </el-form-item>
        <el-form-item label="配置键名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="如：baidu.ai.api-key" :disabled="dialog.isEdit" />
          <div class="form-tip">键名规则：小写字母、数字、点、横线，编辑后不可更改</div>
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input
            v-model="form.configValue"
            :type="formValueVisible ? 'text' : 'password'"
            placeholder="请输入密钥值"
            autocomplete="new-password"
          >
            <i
              slot="suffix"
              :class="formValueVisible ? 'el-icon-unlock' : 'el-icon-lock'"
              class="input-eye"
              @click="formValueVisible = !formValueVisible"
            />
          </el-input>
        </el-form-item>
        <el-form-item label="所属服务" prop="configType">
          <el-select v-model="form.remark" placeholder="选择服务分组（填入备注）" style="width:100%">
            <el-option v-for="g in serviceGroups" :key="g.name" :label="g.label" :value="g.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="系统内置">
          <el-radio-group v-model="form.configType">
            <el-radio label="Y">是</el-radio>
            <el-radio label="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialog.open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listSecrets, updateSecret, addSecret, delSecret, refreshCache } from '@/api/admin/secrets'

const SERVICE_GROUPS = [
  { name: 'database',  label: '数据库 / Redis', color: '#409EFF', keywords: ['db.', 'redis.', 'druid.', 'datasource'] },
  { name: 'jwt',       label: 'JWT 认证',       color: '#67C23A', keywords: ['jwt.', 'token.'] },
  { name: 'baidu',     label: '百度AI（生物识别）', color: '#E6A23C', keywords: ['baidu.'] },
  { name: 'tencent',   label: '腾讯云（混元3D）',  color: '#00B0E6', keywords: ['hunyuan', 'tencent.'] },
  { name: 'qiniu',     label: '七牛云（对象存储）', color: '#8E44AD', keywords: ['qiniu.'] },
  { name: 'wiki',      label: 'Wiki.js 知识库',   color: '#16A085', keywords: ['wiki.'] },
  { name: 'qwen',      label: '千问 AI（DashScope）', color: '#E74C3C', keywords: ['qwen.', 'dashscope.'] },
  { name: 'other',     label: '其他配置',          color: '#909399', keywords: [] }
]

export default {
  name: 'SecretsManagement',
  data() {
    return {
      loading: false,
      allSecrets: [],
      serviceGroups: SERVICE_GROUPS,
      dialog: { open: false, title: '', isEdit: false },
      formValueVisible: false,
      form: {
        configId: null,
        configName: '',
        configKey: '',
        configValue: '',
        configType: 'Y',
        remark: ''
      },
      rules: {
        configName:  [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
        configKey:   [{ required: true, message: '请输入配置键名', trigger: 'blur' }],
        configValue: [{ required: true, message: '请输入配置值',   trigger: 'blur' }]
      }
    }
  },
  computed: {
    groupedSecrets() {
      return SERVICE_GROUPS.map(group => ({
        ...group,
        items: this.allSecrets.filter(item => {
          if (group.name === 'other') {
            return !SERVICE_GROUPS.filter(g => g.name !== 'other')
              .some(g => g.keywords.some(k => (item.configKey || '').toLowerCase().includes(k)))
          }
          return group.keywords.some(k => (item.configKey || '').toLowerCase().includes(k))
        })
      })).filter(g => g.items.length > 0)
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listSecrets().then(res => {
        this.allSecrets = (res.rows || []).map(r => ({ ...r, _visible: false }))
        this.loading = false
      }).catch(() => { this.loading = false })
    },

    maskValue(val) {
      if (!val) return '（未设置）'
      if (val.length <= 6) return '••••••'
      return val.slice(0, 3) + '•'.repeat(Math.min(val.length - 6, 20)) + val.slice(-3)
    },

    toggleVisible(row) {
      row._visible = !row._visible
    },

    handleAdd() {
      this.resetForm()
      this.dialog = { open: true, title: '新增密钥配置', isEdit: false }
      this.formValueVisible = false
    },

    handleEdit(row) {
      this.resetForm()
      this.form = {
        configId:    row.configId,
        configName:  row.configName,
        configKey:   row.configKey,
        configValue: row.configValue,
        configType:  row.configType,
        remark:      row.remark
      }
      this.formValueVisible = false
      this.dialog = { open: true, title: '编辑密钥配置', isEdit: true }
    },

    handleDelete(row) {
      this.$confirm(`确认删除「${row.configName}」？此操作不可恢复。`, '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }).then(() => delSecret(row.configId))
        .then(() => {
          this.$message.success('删除成功')
          this.getList()
        }).catch(() => {})
    },

    handleRefreshCache() {
      refreshCache().then(() => {
        this.$message.success('缓存已刷新')
      })
    },

    submitForm() {
      this.$refs.secretForm.validate(valid => {
        if (!valid) return
        const action = this.dialog.isEdit ? updateSecret : addSecret
        action(this.form).then(() => {
          this.$message.success(this.dialog.isEdit ? '更新成功' : '新增成功')
          this.dialog.open = false
          this.getList()
        })
      })
    },

    resetForm() {
      this.form = { configId: null, configName: '', configKey: '', configValue: '', configType: 'Y', remark: '' }
      this.$nextTick(() => {
        if (this.$refs.secretForm) this.$refs.secretForm.clearValidate()
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.secrets-page {
  background: #f0f2f5;
  min-height: calc(100vh - 84px);
  padding: 20px;
}

/* ── 顶部标题 ── */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);

  &__left {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}
.header-icon {
  font-size: 36px;
  color: #409EFF;
  background: #ecf5ff;
  border-radius: 10px;
  padding: 10px;
}
.page-title {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
}
.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

/* ── 服务分组 ── */
.secret-group {
  background: #fff;
  border-radius: 10px;
  margin-bottom: 20px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
}
.group-header {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafbfc;
}
.group-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 10px;
  flex-shrink: 0;
}
.group-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

/* ── 表格 ── */
.secret-table {
  width: 100%;
}
.config-key {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 2px 7px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
}
.secret-value-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.masked {
  font-family: monospace;
  letter-spacing: 2px;
  color: #c0c4cc;
  font-size: 16px;
}
.revealed {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #303133;
  word-break: break-all;
}
.toggle-btn {
  flex-shrink: 0;
  font-size: 12px;
  padding: 0;
}

/* ── 表单 ── */
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.input-eye {
  cursor: pointer;
  color: #909399;
  &:hover { color: #409EFF; }
}
</style>
