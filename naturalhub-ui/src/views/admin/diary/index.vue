<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="用户名" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="地点" prop="location">
        <el-input
          v-model="queryParams.location"
          placeholder="请输入地点"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="可见性" prop="visibility">
        <el-select v-model="queryParams.visibility" placeholder="请选择可见性" clearable>
          <el-option label="私密" value="0" />
          <el-option label="公开" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="归档状态" prop="isArchived">
        <el-select v-model="queryParams.isArchived" placeholder="请选择归档状态" clearable>
          <el-option label="活跃" value="0" />
          <el-option label="已归档" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="观察时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-folder"
          size="mini"
          :disabled="multiple"
          @click="handleBatchArchive"
          v-hasPermi="['admin:diary:edit']"
        >批量归档</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['admin:diary:remove']"
        >批量删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['admin:diary:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409EFF;">
              <i class="el-icon-notebook-2"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">日志总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67C23A;">
              <i class="el-icon-view"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.publicCount }}</div>
              <div class="stat-label">公开日志</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #909399;">
              <i class="el-icon-lock"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.privateCount }}</div>
              <div class="stat-label">私密日志</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #E6A23C;">
              <i class="el-icon-folder"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.archivedCount }}</div>
              <div class="stat-label">已归档</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="diaryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="diaryId" width="80" />
      <el-table-column label="用户" align="center" prop="userName" width="100" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="150" />
      <el-table-column label="观察地点" align="center" prop="location" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="观察时间" align="center" prop="observationDate" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.observationDate, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="天气" align="center" prop="weather" width="100" />
      <el-table-column label="可见性" align="center" prop="visibility" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.visibility === '0' ? 'info' : 'success'" size="small">
            {{ scope.row.visibility === '0' ? '私密' : '公开' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="归档状态" align="center" prop="isArchived" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isArchived === '1' ? 'warning' : 'success'" size="small">
            {{ scope.row.isArchived === '1' ? '已归档' : '活跃' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['admin:diary:query']"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            :icon="scope.row.isArchived === '1' ? 'el-icon-folder-opened' : 'el-icon-folder'"
            @click="handleArchive(scope.row)"
            v-hasPermi="['admin:diary:edit']"
          >{{ scope.row.isArchived === '1' ? '取消归档' : '归档' }}</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['admin:diary:remove']"
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

    <!-- 查看详情对话框 -->
    <el-dialog :title="'观察日志详情'" :visible.sync="detailOpen" width="900px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ detail.diaryId }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="观察时间">{{ parseTime(detail.observationDate) }}</el-descriptions-item>
        <el-descriptions-item label="观察地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="天气情况">{{ detail.weather || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="温度">{{ detail.temperature || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="可见性">
          <el-tag :type="detail.visibility === '0' ? 'info' : 'success'" size="small">
            {{ detail.visibility === '0' ? '私密' : '公开' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="归档状态">
          <el-tag :type="detail.isArchived === '1' ? 'warning' : 'success'" size="small">
            {{ detail.isArchived === '1' ? '已归档' : '活跃' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="路线信息" :span="2" v-if="detail.routeInfo">{{ detail.routeInfo }}</el-descriptions-item>
        <el-descriptions-item label="发现物种" :span="2" v-if="detail.speciesFound">
          <pre style="white-space: pre-wrap; margin: 0;">{{ detail.speciesFound }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="日志内容" :span="2">
          <div style="white-space: pre-wrap;">{{ detail.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="标签" :span="2" v-if="detail.tags">
          <el-tag
            v-for="tag in getTags(detail.tags)"
            :key="tag"
            size="small"
            style="margin-right: 5px;"
          >
            {{ tag }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="统计数据" :span="2">
          <span style="margin-right: 15px;">
            <i class="el-icon-view"></i> 浏览 {{ detail.viewCount || 0 }}
          </span>
          <span style="margin-right: 15px;">
            <i class="el-icon-thumb"></i> 点赞 {{ detail.likeCount || 0 }}
          </span>
          <span>
            <i class="el-icon-star-off"></i> 收藏 {{ detail.favoriteCount || 0 }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ parseTime(detail.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2" v-if="detail.remark">
          {{ detail.remark }}
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDiary, getDiary, delDiary, archiveDiary, getStatistics } from "@/api/admin/diary";

export default {
  name: "DiaryAdmin",
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
      // 观察日志表格数据
      diaryList: [],
      // 详情对话框
      detailOpen: false,
      detail: {},
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: null,
        title: null,
        location: null,
        visibility: null,
        isArchived: null
      },
      // 统计数据
      stats: {
        total: 0,
        publicCount: 0,
        privateCount: 0,
        archivedCount: 0
      }
    };
  },
  created() {
    this.getList();
    this.getStats();
  },
  methods: {
    /** 查询观察日志列表 */
    getList() {
      this.loading = true;
      listDiary(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.diaryList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 获取统计数据 */
    getStats() {
      getStatistics().then(response => {
        this.stats = response.data || response || {
          total: 0,
          publicCount: 0,
          privateCount: 0,
          archivedCount: 0
        };
      }).catch(() => {
        this.stats = {
          total: 0,
          publicCount: 0,
          privateCount: 0,
          archivedCount: 0
        };
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.diaryId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 查看详情 */
    handleView(row) {
      // 先打开弹窗，保证一定能弹出
      this.detailOpen = true;
      this.detail = row;

      // 再加载数据
      getDiary(row.diaryId).then(res => {
        this.detail = res.data || res || {};
      });
    },
    /** 归档/取消归档按钮 */
    handleArchive(row) {
      const isArchived = row.isArchived === '1' ? '0' : '1';
      const text = isArchived === '1' ? '归档' : '取消归档';
      
      this.$modal.confirm('是否确认' + text + '该日志？').then(() => {
        return archiveDiary(row.diaryId, isArchived);
      }).then(() => {
        this.getList();
        this.getStats();
        this.$modal.msgSuccess(text + '成功');
      }).catch(() => {});
    },
    /** 批量归档 */
    handleBatchArchive() {
      const diaryIds = this.ids;
      this.$modal.confirm('是否确认批量归档选中的' + diaryIds.length + '条日志？').then(function() {
        return archiveDiary(diaryIds.join(','), '1');
      }).then(() => {
        this.getList();
        this.getStats();
        this.$modal.msgSuccess("批量归档成功");
      }).catch(() => {});
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const diaryIds = row.diaryId || this.ids;
      this.$modal.confirm('是否确认删除观察日志编号为"' + diaryIds + '"的数据项？').then(function() {
        return delDiary(diaryIds);
      }).then(() => {
        this.getList();
        this.getStats();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('admin/diary/export', {
        ...this.queryParams
      }, `diary_${new Date().getTime()}.xlsx`);
    },
    /** 获取标签数组 */
    getTags(tags) {
      if (!tags) return [];
      return tags.split(',').filter(tag => tag.trim());
    }
  }
};
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}
</style>
