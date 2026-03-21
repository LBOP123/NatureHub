<template>
  <el-dialog
    title="分享到社群"
    :visible.sync="innerVisible"
    width="560px"
    append-to-body
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="shareForm" label-width="90px">
      <el-form-item label="分享板块">
        <el-input :value="categoryLabel" disabled />
        <div class="tip">{{ categoryTip }}</div>
      </el-form-item>

      <el-form-item label="分享标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入分享标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="分享内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="请输入分享内容（将保存到话题描述）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleConfirm">确认分享</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'ShareDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    sourceType: {
      type: Number,
      required: true
    },
    initTitle: {
      type: String,
      default: ''
    },
    initContent: {
      type: String,
      default: ''
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      form: {
        title: '',
        content: ''
      },
      rules: {
        title: [{ required: true, message: '请输入分享标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入分享内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    innerVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    },
    categoryLabel() {
      const map = {
        1: '观察记录',
        2: '鉴定求助',
        3: '野外调查'
      }
      return map[this.sourceType] || '社群'
    },
    categoryTip() {
      const map = {
        1: '观察记录只能分享到「观察记录」板块',
        2: '物种鉴定只能分享到「鉴定求助」板块',
        3: '野外调查只能分享到「野外调查」板块'
      }
      return map[this.sourceType] || ''
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.form.title = this.initTitle || ''
        this.form.content = this.initContent || ''
        this.$nextTick(() => {
          this.$refs.shareForm && this.$refs.shareForm.clearValidate()
        })
      }
    }
  },
  methods: {
    handleConfirm() {
      this.$refs.shareForm.validate(valid => {
        if (valid) {
          this.$emit('confirm', {
            title: this.form.title,
            content: this.form.content
          })
        }
      })
    },
    handleClose() {
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
