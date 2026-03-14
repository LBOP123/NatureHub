<template>
  <el-dialog title="举报" :visible.sync="visible" width="460px" append-to-body
    @update:visible="$emit('update:visible', $event)">
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="举报原因" prop="reason">
        <el-select v-model="form.reason" placeholder="请选择" style="width:100%">
          <el-option label="垃圾广告" value="0" />
          <el-option label="辱骂攻击" value="1" />
          <el-option label="违法违规" value="2" />
          <el-option label="虚假信息" value="3" />
          <el-option label="其他" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="详细说明" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述举报原因" />
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="handleSubmit">提交</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'ReportDialog',
  props: {
    visible: { type: Boolean, default: false },
    form:    { type: Object, required: true }
  },
  data() {
    return {
      rules: {
        reason:      [{ required: true, message: '请选择举报原因', trigger: 'change' }],
        description: [
          { required: true, message: '请填写详细描述', trigger: 'blur' },
          { min: 10, message: '至少10个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) this.$emit('submit')
      })
    }
  }
}
</script>
