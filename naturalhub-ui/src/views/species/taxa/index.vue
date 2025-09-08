<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="物种学名" prop="scientificName">
        <el-input
          v-model="queryParams.scientificName"
          placeholder="请输入物种学名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物种中文名" prop="chineseName">
        <el-input
          v-model="queryParams.chineseName"
          placeholder="请输入物种中文名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类阶元级别" prop="rankLevel">
        <el-select v-model="queryParams.rankLevel" placeholder="分类阶元级别" clearable>
          <el-option
            v-for="dict in dict.type.species_taxa_ranks"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
<!--      <el-form-item label="父分类单元ID" prop="parentTaxonId">-->
<!--        <el-input-->
<!--          v-model="queryParams.parentTaxonId"-->
<!--          placeholder="请输入父分类单元ID"-->
<!--          clearable-->
<!--          @keyup.enter.native="handleQuery"-->
<!--        />-->
<!--      </el-form-item>-->
      <el-form-item label="单元编码" prop="taxonCode">
        <el-input
          v-model="queryParams.taxonCode"
          placeholder="请输入分类单元编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否有效" prop="isValid">
        <el-input
          v-model="queryParams.isValid"
          placeholder="请输入是否有效"
          clearable
          @keyup.enter.native="handleQuery"
        />
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
          v-hasPermi="['species:taxa:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['species:taxa:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['species:taxa:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['species:taxa:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taxaList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="分类单元ID" align="center" prop="taxonId" />
      <el-table-column label="物种学名" align="center" prop="scientificName" />
      <el-table-column label="物种中文名" align="center" prop="chineseName" />
      <el-table-column label="俗名" align="center" prop="commonNames" />
      <el-table-column label="分类阶元级别" align="center" prop="rankLevel">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.taxonomic_ranks" :value="scope.row.rankLevel"/>
        </template>
      </el-table-column>
      <el-table-column label="父分类单元ID" align="center" prop="parentTaxonId" />
      <el-table-column label="分类单元编码" align="center" prop="taxonCode" />
      <el-table-column label="是否有效" align="center" prop="isValid">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_yes_no" :value="scope.row.isValid"/>
        </template>
      </el-table-column>
      <el-table-column label="同义词" align="center" prop="synonyms" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['species:taxa:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['species:taxa:remove']"
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

    <!-- 添加或修改物种分类基础对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="物种学名" prop="scientificName">
          <el-input v-model="form.scientificName" placeholder="请输入物种学名" />
        </el-form-item>
        <el-form-item label="物种中文名" prop="chineseName">
          <el-input v-model="form.chineseName" placeholder="请输入物种中文名" />
        </el-form-item>
        <el-form-item label="俗名" prop="commonNames">
          <el-input v-model="form.commonNames" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="分类阶元级别" prop="rankLevel">
          <el-input v-model="form.rankLevel" placeholder="请输入分类阶元级别" />
        </el-form-item>
        <el-form-item label="父分类单元ID" prop="parentTaxonId">
          <el-input v-model="form.parentTaxonId" placeholder="请输入父分类单元ID" />
        </el-form-item>
        <el-form-item label="分类单元编码" prop="taxonCode">
          <el-input v-model="form.taxonCode" placeholder="请输入分类单元编码" />
        </el-form-item>
        <el-form-item label="是否有效" prop="isValid">
          <el-input v-model="form.isValid" placeholder="请输入是否有效" />
        </el-form-item>
        <el-form-item label="同义词" prop="synonyms">
          <el-input v-model="form.synonyms" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTaxa, getTaxa, delTaxa, addTaxa, updateTaxa } from "@/api/species/taxa"

export default {
  name: "Taxa",
  dicts: ['species_taxa_ranks', 'sys_yes_no'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 物种分类基础表格数据
      taxaList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        scientificName: null,
        chineseName: null,
        commonNames: null,
        rankLevel: null,
        parentTaxonId: null,
        taxonCode: null,
        isValid: null,
        synonyms: null,
        remark: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        scientificName: [
          { required: true, message: "物种学名不能为空", trigger: "blur" }
        ],
        chineseName: [
          { required: true, message: "物种中文名不能为空", trigger: "blur" }
        ],
        rankLevel: [
          { required: true, message: "分类阶元级别不能为空", trigger: "blur" }
        ],
        isValid: [
          { required: true, message: "是否有效不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询物种分类基础列表 */
    getList() {
      this.loading = true
      listTaxa(this.queryParams).then(response => {
        this.taxaList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        taxonId: null,
        scientificName: null,
        chineseName: null,
        commonNames: null,
        rankLevel: null,
        parentTaxonId: null,
        taxonCode: null,
        isValid: null,
        synonyms: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.taxonId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加物种分类基础"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const taxonId = row.taxonId || this.ids
      getTaxa(taxonId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改物种分类基础"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.taxonId != null) {
            updateTaxa(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addTaxa(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const taxonIds = row.taxonId || this.ids
      this.$modal.confirm('是否确认删除物种分类基础编号为"' + taxonIds + '"的数据项？').then(function() {
        return delTaxa(taxonIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('species/taxa/export', {
        ...this.queryParams
      }, `taxa_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
