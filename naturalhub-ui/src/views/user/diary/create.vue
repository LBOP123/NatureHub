<template>
  <div class="diary-create-container">
    <el-card class="create-card">
      <div slot="header" class="card-header">
        <i class="el-icon-edit"></i>
        <span>{{ form.diaryId ? '编辑观察日志' : '新建观察日志' }}</span>
      </div>

      <el-form ref="diaryForm" :model="form" :rules="rules" label-width="100px" class="diary-form">
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="日志标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="请输入日志标题" 
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="观察日期" prop="observationDate">
          <el-date-picker
            v-model="form.observationDate"
            type="datetime"
            placeholder="选择观察日期"
            value-format="yyyy-MM-dd HH:mm:ss"
            :picker-options="pickerOptions"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="观察地点" prop="location">
          <el-input 
            v-model="form.location" 
            placeholder="请输入观察地点"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 环境信息 -->
        <el-divider content-position="left">环境信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="天气情况">
              <el-input 
                v-model="form.weather" 
                placeholder="如：晴朗、多云、小雨"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="温度">
              <el-input 
                v-model="form.temperature" 
                placeholder="如：15-20℃"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="路线信息">
          <el-input
            v-model="form.routeInfo"
            type="textarea"
            :rows="3"
            placeholder="描述你的探索路线，如：从公园东门进入，沿湖边小路前行..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 观察内容 -->
        <el-divider content-position="left">观察内容</el-divider>

        <el-form-item label="发现物种">
          <el-input
            v-model="form.speciesFound"
            type="textarea"
            :rows="4"
            placeholder="记录发现的物种，如：白头鹎 3只、喜鹊 2只、迎春花 多处"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="日志内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="记录你的观察心得、发现和感悟..."
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <!-- 其他信息 -->
        <el-divider content-position="left">其他信息</el-divider>

        <el-form-item label="标签">
          <el-input
            v-model="form.tags"
            placeholder="多个标签用逗号分隔，如：春季,鸟类,植物"
            maxlength="200"
          />
          <div class="form-tip">
            <i class="el-icon-info"></i> 添加标签可以帮助你更好地分类和查找日志
          </div>
        </el-form-item>

        <el-form-item label="可见性" prop="visibility">
          <el-radio-group v-model="form.visibility">
            <el-radio label="0">
              <i class="el-icon-lock"></i> 私密（仅自己可见）
            </el-radio>
            <el-radio label="1">
              <i class="el-icon-view"></i> 公开（所有人可见）
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="其他补充信息"
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">
            <i class="el-icon-check"></i> 保存日志
          </el-button>
          <el-button @click="resetForm">
            <i class="el-icon-refresh-left"></i> 重置
          </el-button>
          <el-button @click="goBack">
            <i class="el-icon-back"></i> 返回
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getDiary, addDiary, updateDiary } from "@/api/user/diary";

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
        remark: null
      },
      rules: {
        title: [
          { required: true, message: "日志标题不能为空", trigger: "blur" },
          { min: 2, max: 200, message: "标题长度在2到200个字符", trigger: "blur" }
        ],
        observationDate: [
          { required: true, message: "请选择观察日期", trigger: "change" }
        ],
        location: [
          { required: true, message: "请输入观察地点", trigger: "blur" }
        ],
        content: [
          { required: true, message: "请输入日志内容", trigger: "blur" },
          { min: 10, max: 5000, message: "内容长度在10到5000个字符", trigger: "blur" }
        ],
        visibility: [
          { required: true, message: "请选择可见性", trigger: "change" }
        ]
      },
      submitLoading: false,
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      }
    };
  },
  created() {
    const diaryId = this.$route.query.id;
    if (diaryId) {
      this.loadDiaryData(diaryId);
    }
  },
  methods: {
    loadDiaryData(diaryId) {
      getDiary(diaryId).then(response => {
        this.form = response.data;
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
    },
    goBack() {
      this.$router.go(-1);
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
  }
}

// 移动端适配
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
