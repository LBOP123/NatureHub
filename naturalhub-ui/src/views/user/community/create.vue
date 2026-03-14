<template>
  <div class="community-create-container">
    <el-card class="create-card">
      <div slot="header" class="create-header">
        <el-button icon="el-icon-back" size="small" @click="goBack">返回</el-button>
        <span class="header-title">发布话题</span>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="100px" class="create-form">
        <el-form-item label="话题分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择话题分类" style="width:100%">
            <el-option
              v-for="item in categoryList"
              :key="item.categoryCode"
              :label="item.categoryIcon + ' ' + item.categoryName"
              :value="item.categoryCode"
            >
              <span style="font-size: 18px;">{{ item.categoryIcon }}</span>
              <span style="margin-left: 10px;">{{ item.categoryName }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="话题标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入话题标题" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="话题内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="分享你的观察、经验或问题..."
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-input
            v-model="form.tags"
            placeholder="多个标签用逗号分隔，如：鸟类,观察,北京"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">发 布</el-button>
          <el-button @click="goBack">取 消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { addTopic } from '@/api/user/community'
import { listAllCategory } from '@/api/community/category'

export default {
  name: 'CommunityCreate',
  data() {
    return {
      submitting: false,
      categoryList: [],
      form: {
        topicId: null,
        category: 'species_science',
        title: null,
        content: null,
        tags: null
      },
      rules: {
        category: [
          { required: true, message: '请选择话题分类', trigger: 'change' }
        ],
        title: [
          { required: true, message: '话题标题不能为空', trigger: 'blur' },
          { min: 5, max: 200, message: '标题长度在 5 到 200 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '话题内容不能为空', trigger: 'blur' },
          { min: 10, message: '内容至少10个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getCategoryList()
  },
  methods: {
    getCategoryList() {
      listAllCategory().then(response => {
        this.categoryList = response.data
      })
    },
    goBack() {
      this.$router.back()
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          this.submitting = true
          addTopic(this.form).then(() => {
            this.$message.success('话题已提交，等待管理员审核后显示')
            this.$router.push({ path: '/user/community/list' })
          }).finally(() => {
            this.submitting = false
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.community-create-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.create-card {
  border-radius: 12px;
}

.create-header {
  display: flex;
  align-items: center;
  gap: 16px;

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
  }
}

.create-form {
  margin-top: 10px;
}
</style>
