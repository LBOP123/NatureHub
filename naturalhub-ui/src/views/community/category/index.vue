<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="板块名称" prop="categoryName">
        <el-input
          v-model="queryParams.categoryName"
          placeholder="请输入板块名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['community:category:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['community:category:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['community:category:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="categoryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="板块ID" align="center" prop="categoryId" width="80" />
      <el-table-column label="板块代码" align="center" prop="categoryCode" width="150" />
      <el-table-column label="板块名称" align="center" prop="categoryName" width="150" />
      <el-table-column label="板块图标" align="center" prop="categoryIcon" width="100">
        <template slot-scope="scope">
          <span style="font-size: 24px;">{{ scope.row.categoryIcon }}</span>
        </template>
      </el-table-column>
      <el-table-column label="板块描述" align="center" prop="categoryDesc" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="排序" align="center" prop="sortOrder" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['community:category:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['community:category:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改板块对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="板块代码" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="请输入板块代码（唯一标识）" :disabled="form.categoryId != null" />
          <span class="form-tip">板块代码用于系统识别，创建后不可修改</span>
        </el-form-item>
        <el-form-item label="板块名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入板块名称" />
        </el-form-item>
        <el-form-item label="板块图标" prop="categoryIcon">
          <el-input v-model="form.categoryIcon" placeholder="请输入板块图标（emoji）" maxlength="10">
            <template slot="append">
              <span style="font-size: 20px;">{{ form.categoryIcon }}</span>
            </template>
          </el-input>
          <span class="form-tip">建议使用emoji表情，如：🔬 🌲 🔍 🐾 🌿</span>
        </el-form-item>
        <el-form-item label="板块描述" prop="categoryDesc">
          <el-input v-model="form.categoryDesc" type="textarea" :rows="3" placeholder="请输入板块描述" />
        </el-form-item>
        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
          <span class="form-tip">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCategory, getCategory, addCategory, updateCategory, delCategory, exportCategory } from "@/api/community/category"

export default {
  name: "CommunityCategory",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      categoryList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        categoryName: null,
        status: null
      },
      form: {},
      rules: {
        categoryCode: [
          { required: true, message: "板块代码不能为空", trigger: "blur" },
          { pattern: /^[a-z_]+$/, message: "板块代码只能包含小写字母和下划线", trigger: "blur" }
        ],
        categoryName: [
          { required: true, message: "板块名称不能为空", trigger: "blur" }
        ],
        categoryIcon: [
          { required: true, message: "板块图标不能为空", trigger: "blur" }
        ],
        sortOrder: [
          { required: true, message: "排序顺序不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listCategory(this.queryParams).then(response => {
        this.categoryList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        categoryId: null,
        categoryCode: null,
        categoryName: null,
        categoryIcon: null,
        categoryDesc: null,
        sortOrder: 0,
        status: "0",
        remark: null
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.categoryId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加板块分类"
    },
    handleUpdate(row) {
      this.reset()
      const categoryId = row.categoryId || this.ids
      getCategory(categoryId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改板块分类"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.categoryId != null) {
            updateCategory(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addCategory(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用"
      this.$modal.confirm('确认要"' + text + '""' + row.categoryName + '"板块吗？').then(() => {
        return updateCategory(row)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(() => {
        row.status = row.status === "0" ? "1" : "0"
      })
    },
    handleDelete(row) {
      const categoryIds = row.categoryId || this.ids
      this.$modal.confirm('是否确认删除板块编号为"' + categoryIds + '"的数据项？').then(() => {
        return delCategory(categoryIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('community/category/export', {
        ...this.queryParams
      }, `板块分类_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style lang="scss" scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}
</style>
