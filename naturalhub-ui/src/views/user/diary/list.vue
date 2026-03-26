<template>
  <div class="diary-list-container">
    <div class="page-header">
      <h1 class="page-title">我的观察日志</h1>
      <p class="page-subtitle">记录每一次与自然的相遇</p>
    </div>

    <div class="search-wrapper">
      <el-input
        v-model="queryParams.title"
        placeholder="搜索日志..."
        prefix-icon="el-icon-search"
        clearable
        @keyup.enter.native="handleQuery"
        class="search-input"
      />
      <el-button type="primary" @click="handleQuery" class="search-btn">搜索</el-button>
      <el-button type="success" icon="el-icon-plus" @click="handleAdd" class="add-btn">新增日志</el-button>
    </div>

    <div v-loading="loading" class="content-area">
      <div class="list-table">
        <div v-if="diaryList.length > 0" class="list-content">
          <div
            v-for="item in diaryList"
            :key="item.diaryId"
            class="list-item"
            @click="handleDetail(item)"
          >
            <div class="col-icon row-icon">
              <i class="el-icon-document"></i>
            </div>

            <div class="col-title">
              <div class="title-wrap">
                <span class="diary-title" @click.stop="handleUpdate(item)">{{ item.title }}</span>
                <span class="edit-icon" @click.stop="handleUpdate(item)">
                  <i class="el-icon-edit"></i>
                </span>
                <el-tag v-if="item.isArchived === '1'" size="mini" type="warning" class="archive-tag">
                  已归档
                </el-tag>
              </div>
            </div>

            <div class="col-location">
              {{ item.location || '-' }}
            </div>

            <div class="col-time">
              {{ formatDateTime(item.observationDate) }}
            </div>

            <div class="col-action" @click.stop>
              <el-dropdown trigger="click" @command="handleCommand($event, item)">
                <span class="more-btn">
                  <i class="el-icon-more"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="archive">{{ item.isArchived === '1' ? '取消归档' : '归档' }}</el-dropdown-item>
                  <el-dropdown-item command="view">查看</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="diaryList.length === 0" description="暂无日志记录" class="empty-state">
        <el-button type="primary" @click="handleAdd">立即创建</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script>
import { listDiary, delDiary, archiveDiary } from "@/api/user/diary";

export default {
  name: "DiaryList",
  data() {
    return {
      loading: true,
      total: 0,
      diaryList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        title: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    formatDateTime(dateStr) {
      if (!dateStr) return "-";
      const date = new Date(dateStr);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const hours = date.getHours().toString().padStart(2, "0");
      const minutes = date.getMinutes().toString().padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
    getList() {
      this.loading = true;
      listDiary(this.queryParams)
        .then(response => {
          this.diaryList = response.rows || [];
          this.total = response.total;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    handleAdd() {
      this.$router.push("/user/diary/create");
    },
    handleUpdate(row) {
      if (row.isArchived === "1") {
        this.$modal.msgWarning("该日志已归档，无法编辑!");
        return;
      }
      this.$router.push({ path: "/user/diary/create", query: { id: row.diaryId } });
    },
    handleDetail(row) {
      this.$router.push({ path: "/user/diary/detail", query: { id: row.diaryId } });
    },
    handleArchive(row) {
      const isArchived = row.isArchived === "1" ? "0" : "1";
      const text = isArchived === "1" ? "归档" : "取消归档";
      this.$modal
        .confirm("是否确认" + text + "该日志?")
        .then(() => {
          return archiveDiary(row.diaryId, isArchived);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess(text + "成功");
        })
        .catch(() => {});
    },
    handleDelete(row) {
      this.$modal
        .confirm("是否确认删除该日志?")
        .then(() => {
          return delDiary(row.diaryId);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    },
    handleCommand(command, row) {
      if (command === "edit") {
        this.handleUpdate(row);
      } else if (command === "archive") {
        this.handleArchive(row);
      } else if (command === "view") {
        this.handleDetail(row);
      } else if (command === "delete") {
        this.handleDelete(row);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.diary-list-container {
  padding: 20px 30px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 20px;
  padding-top: 10px;

  .page-title {
    font-size: 28px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
  }

  .page-subtitle {
    font-size: 14px;
    color: #999;
    margin: 0;
  }
}

.search-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 auto 20px;
  max-width: 1200px;

  .search-input {
    flex: 1;
    height: 44px;
  }

  .search-btn,
  .add-btn {
    height: 44px;
    padding: 0 20px;
    white-space: nowrap;
  }
}

.content-area {
  max-width: 1200px;
  margin: 0 auto;
}

.list-table {
  border: 1px solid #e7e7e7;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.list-item {
  display: grid;
  grid-template-columns: 90px minmax(260px, 1.4fr) minmax(180px, 1fr) minmax(200px, 1fr) 70px;
  align-items: center;
}

.list-content {
  .list-item {
    min-height: 72px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background 0.2s;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #fafcff;
    }
  }
}

.col-icon,
.col-title,
.col-location,
.col-time,
.col-action {
  padding: 0 16px;
}

.row-icon {
  color: #5a8dee;
  font-size: 20px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;

  .diary-title {
    color: #303133;
    font-size: 14px;
    font-weight: 600;
    max-width: 360px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &:hover {
      color: #409eff;
      text-decoration: underline;
    }
  }

  .edit-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #409eff;
    font-size: 16px;
    cursor: pointer;
    flex-shrink: 0;

    &:hover {
      color: #1f78d1;
    }
  }

  .archive-tag {
    flex-shrink: 0;
  }
}

.col-location,
.col-time {
  color: #606266;
  font-size: 13px;
}

.col-action {
  text-align: center;
}

.more-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  color: #606266;
  transition: all 0.2s;

  &:hover {
    background: #f1f3f7;
    color: #303133;
  }

  i {
    font-size: 16px;
  }
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

@media (max-width: 900px) {
  .diary-list-container {
    padding: 15px 10px;
  }

  .search-wrapper {
    flex-wrap: wrap;

    .search-input {
      width: 100%;
    }

    .search-btn,
    .add-btn {
      flex: 1;
    }
  }

  .list-item {
    grid-template-columns: 64px minmax(180px, 1.2fr) minmax(130px, 1fr) minmax(140px, 1fr) 52px;
  }
}
</style>
