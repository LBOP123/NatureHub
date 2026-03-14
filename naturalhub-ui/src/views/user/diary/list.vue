<template>
  <div class="diary-list-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入标题"
            clearable
            size="small"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input
            v-model="queryParams.location"
            placeholder="请输入地点"
            clearable
            size="small"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="可见性" prop="visibility">
          <el-select v-model="queryParams.visibility" placeholder="请选择" clearable size="small">
            <el-option label="私密" value="0" />
            <el-option label="公开" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="归档状态" prop="isArchived">
          <el-select v-model="queryParams.isArchived" placeholder="请选择" clearable size="small">
            <el-option label="活跃" value="0" />
            <el-option label="已归档" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="toolbar-card" shadow="never">
      <el-row :gutter="10">
        <el-col :span="1.5">
          <el-button
            type="primary"
            icon="el-icon-plus"
            size="small"
            @click="handleAdd"
          >新建日志</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="success"
            icon="el-icon-download"
            size="small"
            @click="handleExport"
          >导出</el-button>
        </el-col>
        <el-col :span="1.5" class="view-switch">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="card">
              <i class="el-icon-menu"></i> 卡片
            </el-radio-button>
            <el-radio-button label="list">
              <i class="el-icon-s-operation"></i> 列表
            </el-radio-button>
          </el-radio-group>
        </el-col>
      </el-row>
    </el-card>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'card'" class="card-view">
      <el-row :gutter="20">
        <el-col 
          v-for="item in diaryList" 
          :key="item.diaryId"
          :xs="24" 
          :sm="12" 
          :md="8" 
          :lg="6"
          class="card-col"
        >
          <el-card class="diary-card" :body-style="{ padding: '0px' }" shadow="hover">
            <!-- 卡片头部 -->
            <div class="card-header" @click="handleDetail(item)">
              <div class="date-badge">
                <div class="date-day">{{ getDay(item.observationDate) }}</div>
                <div class="date-month">{{ getMonth(item.observationDate) }}</div>
              </div>
              <!-- 状态标签 -->
              <div class="status-tags">
                <el-tag v-if="item.visibility === '0'" type="info" size="small" effect="dark">私密</el-tag>
                <el-tag v-else type="success" size="small" effect="dark">公开</el-tag>
                <el-tag v-if="item.isArchived === '1'" type="warning" size="small" effect="plain">已归档</el-tag>
              </div>
            </div>
            
            <!-- 卡片内容 -->
            <div class="card-content">
              <div class="card-title" @click="handleDetail(item)">{{ item.title }}</div>
              <div class="card-info">
                <div class="info-item" v-if="item.location">
                  <i class="el-icon-location-outline"></i>
                  <span>{{ item.location }}</span>
                </div>
                <div class="info-item" v-if="item.weather">
                  <i class="el-icon-sunny"></i>
                  <span>{{ item.weather }}</span>
                </div>
                <div class="info-item" v-if="item.temperature">
                  <i class="el-icon-thermometer"></i>
                  <span>{{ item.temperature }}</span>
                </div>
              </div>
              
              <div class="card-excerpt">
                {{ truncate(item.content, 100) }}
              </div>

              <div class="card-tags" v-if="item.tags">
                <el-tag
                  v-for="tag in getTags(item.tags).slice(0, 3)"
                  :key="tag"
                  size="mini"
                  type="info"
                >
                  {{ tag }}
                </el-tag>
              </div>
              
              <!-- 操作按钮 -->
              <div class="card-actions">
                <el-button-group>
                  <el-button size="mini" icon="el-icon-view" @click="handleDetail(item)">查看</el-button>
                  <el-button 
                    v-if="item.isArchived !== '1'"
                    size="mini" 
                    icon="el-icon-edit" 
                    @click="handleUpdate(item)"
                  >编辑</el-button>
                  <el-button 
                    size="mini" 
                    :icon="item.isArchived === '1' ? 'el-icon-folder-opened' : 'el-icon-folder'"
                    @click="handleArchive(item)"
                  >{{ item.isArchived === '1' ? '取消归档' : '归档' }}</el-button>
                  <el-button 
                    size="mini" 
                    type="danger"
                    icon="el-icon-delete" 
                    @click="handleDelete(item)"
                  >删除</el-button>
                </el-button-group>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 空状态 -->
      <el-empty v-if="diaryList.length === 0" description="暂无观察日志">
        <el-button type="primary" @click="handleAdd">立即创建</el-button>
      </el-empty>
    </div>

    <!-- 列表视图 -->
    <el-card v-else class="list-view" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="diaryList"
        @row-click="handleDetail"
        style="width: 100%"
      >
        <el-table-column label="标题" prop="title" min-width="150" show-overflow-tooltip />
        
        <el-table-column label="观察日期" width="120">
          <template slot-scope="scope">
            {{ parseTime(scope.row.observationDate, '{y}-{m}-{d}') }}
          </template>
        </el-table-column>
        
        <el-table-column label="观察地点" prop="location" min-width="120" show-overflow-tooltip />
        
        <el-table-column label="天气" prop="weather" width="100" />
        
        <el-table-column label="可见性" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.visibility === '0'" type="info" size="small">私密</el-tag>
            <el-tag v-else type="success" size="small">公开</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isArchived === '1'" type="warning" size="small">已归档</el-tag>
            <el-tag v-else type="success" size="small">活跃</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="创建时间" width="160">
          <template slot-scope="scope">
            {{ parseTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleDetail(scope.row)">查看</el-button>
            <el-button 
              v-if="scope.row.isArchived !== '1'"
              size="mini" 
              type="text" 
              icon="el-icon-edit" 
              @click.stop="handleUpdate(scope.row)"
            >编辑</el-button>
            <el-button 
              size="mini" 
              type="text" 
              :icon="scope.row.isArchived === '1' ? 'el-icon-folder-opened' : 'el-icon-folder'"
              @click.stop="handleArchive(scope.row)"
            >{{ scope.row.isArchived === '1' ? '取消归档' : '归档' }}</el-button>
            <el-button 
              size="mini" 
              type="text" 
              icon="el-icon-delete" 
              @click.stop="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页 -->
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
import { listDiary, delDiary, archiveDiary } from "@/api/user/diary";

export default {
  name: "DiaryList",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      diaryList: [],
      viewMode: 'card',
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        title: null,
        location: null,
        visibility: null,
        isArchived: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listDiary(this.queryParams).then(response => {
        this.diaryList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleAdd() {
      this.$router.push("/user/diary/create");
    },
    handleUpdate(row) {
      if (row.isArchived === '1') {
        this.$modal.msgWarning('该日志已归档，无法编辑！');
        return;
      }
      this.$router.push({
        path: "/user/diary/create",
        query: { id: row.diaryId }
      });
    },
    handleDetail(row) {
      this.$router.push({
        path: "/user/diary/detail",
        query: { id: row.diaryId }
      });
    },
    handleArchive(row) {
      const isArchived = row.isArchived === '1' ? '0' : '1';
      const text = isArchived === '1' ? '归档' : '取消归档';
      this.$modal.confirm('是否确认' + text + '该日志？').then(() => {
        return archiveDiary(row.diaryId, isArchived);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(text + '成功');
      }).catch(() => {});
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除该观察日志？').then(() => {
        return delDiary(row.diaryId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('user/diary/export', {
        ...this.queryParams
      }, `diary_${new Date().getTime()}.xlsx`);
    },
    truncate(value, length) {
      if (!value) return '';
      if (value.length <= length) return value;
      return value.substring(0, length) + '...';
    },
    getTags(tags) {
      if (!tags) return [];
      return tags.split(',');
    },
    getDay(date) {
      if (!date) return '';
      return this.parseTime(date, '{d}');
    },
    getMonth(date) {
      if (!date) return '';
      return this.parseTime(date, '{m}月');
    }
  }
};
</script>

<style lang="scss" scoped>
.diary-list-container {
  padding: 20px;
  
  .search-card,
  .toolbar-card {
    margin-bottom: 20px;
  }
  
  .view-switch {
    float: right;
  }
  
  // 卡片视图样式
  .card-view {
    .card-col {
      margin-bottom: 20px;
    }
    
    .diary-card {
      height: 100%;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-5px);
      }
      
      .card-header {
        position: relative;
        height: 120px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        
        .date-badge {
          text-align: center;
          color: white;
          
          .date-day {
            font-size: 36px;
            font-weight: bold;
            line-height: 1;
          }
          
          .date-month {
            font-size: 14px;
            margin-top: 5px;
          }
        }
        
        .status-tags {
          position: absolute;
          top: 10px;
          right: 10px;
          display: flex;
          gap: 5px;
        }
      }
      
      .card-content {
        padding: 15px;
        
        .card-title {
          font-size: 16px;
          font-weight: bold;
          margin-bottom: 10px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          color: #303133;
          
          &:hover {
            color: #409EFF;
          }
        }
        
        .card-info {
          margin-bottom: 10px;
          
          .info-item {
            display: flex;
            align-items: center;
            margin-bottom: 5px;
            font-size: 13px;
            color: #606266;
            
            i {
              margin-right: 5px;
              color: #909399;
            }
          }
        }
        
        .card-excerpt {
          font-size: 13px;
          color: #909399;
          line-height: 1.6;
          margin-bottom: 10px;
          min-height: 40px;
        }
        
        .card-tags {
          margin-bottom: 15px;
          
          .el-tag {
            margin-right: 5px;
          }
        }
        
        .card-actions {
          border-top: 1px solid #ebeef5;
          padding-top: 10px;
          
          .el-button-group {
            display: flex;
            flex-wrap: wrap;
            gap: 5px;
          }
        }
      }
    }
  }
  
  // 列表视图样式
  .list-view {
    // 样式继承自全局
  }
}

// 移动端适配
@media screen and (max-width: 768px) {
  .diary-list-container {
    padding: 10px;
    
    .view-switch {
      float: none;
      margin-top: 10px;
    }
    
    .card-view {
      .diary-card {
        .card-header {
          height: 100px;
          
          .date-badge .date-day {
            font-size: 28px;
          }
        }
      }
    }
  }
}
</style>
