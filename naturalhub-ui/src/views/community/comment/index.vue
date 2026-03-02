<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="话题ID" prop="topicId">
        <el-input
          v-model="queryParams.topicId"
          placeholder="请输入话题ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="评论用户" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="已删除" value="1" />
          <el-option label="已屏蔽" value="2" />
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
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['community:comment:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['community:comment:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="commentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="评论ID" align="center" prop="commentId" width="80" />
      <el-table-column label="话题ID" align="center" prop="topicId" width="80" />
      <el-table-column label="评论用户" align="center" prop="userName" width="120" />
      <el-table-column label="评论内容" align="center" prop="content" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="点赞数" align="center" prop="likeCount" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="danger">已删除</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="warning">已屏蔽</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评论时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-lock"
            @click="handleBlock(scope.row)"
            v-hasPermi="['community:comment:edit']"
          >屏蔽</el-button>
          <el-button
            v-if="scope.row.status === '2'"
            size="mini"
            type="text"
            icon="el-icon-unlock"
            @click="handleRestore(scope.row)"
            v-hasPermi="['community:comment:edit']"
          >恢复</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['community:comment:remove']"
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
  </div>
</template>

<script>
import { listComment, delComment, blockComment, restoreComment, exportComment } from "@/api/community/comment"

export default {
  name: "CommunityComment",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      commentList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        topicId: null,
        userName: null,
        status: null
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listComment(this.queryParams).then(response => {
        this.commentList = response.rows
        this.total = response.total
        this.loading = false
      })
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
      this.ids = selection.map(item => item.commentId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleBlock(row) {
      this.$modal.confirm('是否确认屏蔽该评论？').then(() => {
        return blockComment(row.commentId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("屏蔽成功")
      }).catch(() => {})
    },
    handleRestore(row) {
      this.$modal.confirm('是否确认恢复该评论？').then(() => {
        return restoreComment(row.commentId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("恢复成功")
      }).catch(() => {})
    },
    handleDelete(row) {
      const commentIds = row.commentId || this.ids
      this.$modal.confirm('是否确认删除评论编号为"' + commentIds + '"的数据项？').then(() => {
        return delComment(commentIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('community/comment/export', {
        ...this.queryParams
      }, `社群评论_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
