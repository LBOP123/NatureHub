<template>
  <div class="diary-list-container">
    <div class="view-switch-top">
      <el-button :class="['view-btn', { active: viewType === 'grid' }]" @click="viewType = 'grid'"><i class="el-icon-menu"></i> 网格</el-button>
      <el-button :class="['view-btn', { active: viewType === 'list' }]" @click="viewType = 'list'"><i class="el-icon-tickets"></i> 列表</el-button>
    </div>

    <div class="page-header">
      <h1 class="page-title">我的观察日志</h1>
      <p class="page-subtitle">记录每一次与自然的相遇</p>
    </div>

    <div class="search-wrapper">
      <el-input v-model="queryParams.title" placeholder="搜索日志..." prefix-icon="el-icon-search" clearable @keyup.enter.native="handleQuery" class="search-input"/>
      <el-button type="primary" @click="handleQuery" class="search-btn">搜索</el-button>
      <el-button type="success" icon="el-icon-plus" @click="handleAdd" class="add-btn">新增日志</el-button>
    </div>

    <div v-loading="loading" class="content-area">
      <div v-if="viewType === 'grid' && diaryList.length > 0" class="grid-view">
        <div v-for="item in diaryList" :key="item.diaryId" class="grid-card" @click="handleDetail(item)">
          <div class="card-image-wrapper">
            <img v-if="item.coverImage" :src="item.coverImage" class="card-image" alt="diary">
            <div v-else class="card-image-placeholder"><i class="el-icon-picture-outline"></i></div>
            <div class="card-tag"><el-tag v-if="item.visibility === '0'" type="info" size="small" effect="dark">私密</el-tag><el-tag v-else type="success" size="small" effect="dark">公开</el-tag></div>
            <div class="card-tag-left" v-if="item.isArchived === '1'"><el-tag type="warning" size="small">已归档</el-tag></div>
          </div>
          <div class="card-info">
            <div class="card-title">{{ item.title }}</div>
            <div class="info-item" v-if="item.location"><span class="label">地点：</span><span class="info-value">{{ item.location }}</span></div>
            <div class="info-item"><span class="label">日期：</span><span class="info-value">{{ formatGridDateTime(item.observationDate) }}</span></div>
            <div class="info-item"><span class="label">观察记录数：</span><span class="info-value">{{ item.recordCount }}</span></div>
            <div class="card-actions">
              <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleDetail(item)">查看</el-button>
              <el-button v-if="item.isArchived !== '1'" size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(item)">编辑</el-button>
              <el-button size="mini" type="text" :icon="item.isArchived === '1' ? 'el-icon-folder-opened' : 'el-icon-folder'" @click.stop="handleArchive(item)">{{ item.isArchived === '1' ? '取消' : '归档' }}</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="viewType === 'list'">
        <div class="list-header">
          <div class="header-col col-1">媒体</div>
          <div class="header-col col-2">标题</div>
          <div class="header-col col-3">地点</div>
          <div class="header-col col-4">日期</div>
          <div class="header-col col-5">操作</div>
        </div>
        <div v-if="diaryList.length > 0" class="list-content">
          <div v-for="(item, index) in diaryList" :key="item.diaryId" class="list-item" :class="{ 'gray-row': index % 2 === 1 }" @click="handleDetail(item)">
            <div class="list-col col-1">
              <div class="list-image">
                <img v-if="item.coverImage" :src="item.coverImage" alt="diary">
                <div v-else class="list-image-placeholder"><i class="el-icon-picture-outline"></i></div>
              </div>
            </div>
            <div class="list-col col-2"><div class="diary-title">{{ item.title }}</div></div>
            <div class="list-col col-3"><div class="diary-location">{{ item.location || '-' }}</div></div>
            <div class="list-col col-4"><div class="diary-date">{{ formatDatePart(item.observationDate) }}<br/>{{ formatTimePart(item.observationDate) }}</div></div>
            <div class="list-col col-5">
              <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleDetail(item)">查看</el-button>
              <el-button v-if="item.isArchived !== '1'" size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(item)">编辑</el-button>
              <el-button size="mini" type="text" :icon="item.isArchived === '1' ? 'el-icon-folder-opened' : 'el-icon-folder'" @click.stop="handleArchive(item)">{{ item.isArchived === '1' ? '取消' : '归档' }}</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="diaryList.length === 0" description="暂无日志记录" class="empty-state"><el-button type="primary" @click="handleAdd">立即创建</el-button></el-empty>
      </div>
      <el-empty v-if="viewType === 'grid' && diaryList.length === 0" description="暂无日志记录" class="empty-state"><el-button type="primary" @click="handleAdd">立即创建</el-button></el-empty>
    </div>
  </div>
</template>

<script>
import { listDiary, delDiary, archiveDiary } from "@/api/user/diary";
export default {
  name: "DiaryList",
  data() {
    return {
      viewType: 'grid',
      loading: true,
      total: 0,
      diaryList: [],
      queryParams: { pageNum: 1, pageSize: 12, title: null }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    formatGridDateTime(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    formatDatePart(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`
    },
    formatTimePart(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      let hours = date.getHours()
      const ampm = hours >= 12 ? 'PM' : 'AM'
      hours = hours % 12 || 12
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}:${minutes} ${ampm}`
    },
    getList() {
      this.loading = true;
      listDiary(this.queryParams).then(response => {
        this.diaryList = response.rows.map(item => {
          if (item.images) {
            try {
              let images = item.images
              if (typeof images === 'string') {
                if (images.startsWith('[')) {
                  images = JSON.parse(images)
                } else {
                  images = images.split(',').map(s => s.trim()).filter(Boolean)
                }
              }
              item.coverImage = Array.isArray(images) && images.length > 0 ? images[0] : null
            } catch (e) {
              item.coverImage = null
            }
          }
          return item
        });
        this.total = response.total;
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
      if (row.isArchived === '1') {
        this.$modal.msgWarning('该日志已归档，无法编辑!');
        return;
      }
      this.$router.push({ path: "/user/diary/create", query: { id: row.diaryId } });
    },
    handleDetail(row) {
      this.$router.push({ path: "/user/diary/detail", query: { id: row.diaryId } });
    },
    handleArchive(row) {
      const isArchived = row.isArchived === '1' ? '0' : '1';
      const text = isArchived === '1' ? '归档' : '取消归档';
      this.$modal.confirm('是否确认' + text + '该日志?').then(() => {
        return archiveDiary(row.diaryId, isArchived);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(text + '成功');
      }).catch(() => {});
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除该日志?').then(() => {
        return delDiary(row.diaryId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>

<style lang="scss" scoped>
.diary-list-container { padding: 20px 30px; background: #f5f5f5; min-height: 100vh; position: relative; }
.view-switch-top { position: absolute; top: 20px; left: 30px; display: flex; z-index: 10; }
.view-btn { height: 36px; width: 80px; border: 1px solid #ddd; background: #fff; color: #666; font-size: 14px; &.active { background: #409eff; color: #fff; border-color: #409eff; } &:first-child { border-radius: 4px 0 0 4px; border-right: none; } &:last-child { border-radius: 0 4px 4px 0; } }
.page-header { text-align: center; margin-bottom: 20px; padding-top: 20px; .page-title { font-size: 28px; font-weight: 600; color: #333; margin: 0 0 8px 0; } .page-subtitle { font-size: 14px; color: #999; margin: 0; } }
.search-wrapper { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; max-width: 1200px; margin-left: auto; margin-right: auto; width: 100%; .search-input { flex: 1; height: 44px; ::v-deep .el-input__prefix { display: flex; align-items: center; top: 50%; transform: translateY(-50%); } } .search-btn, .add-btn { height: 44px; padding: 0 20px; white-space: nowrap; flex-shrink: 0; } }
.content-area { margin-bottom: 20px; }
.grid-view { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.grid-card { background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.08); transition: all 0.2s; cursor: pointer; &:hover { transform: translateY(-2px); box-shadow: 0 3px 8px rgba(0,0,0,0.12); } }
.card-image-wrapper { width: 100%; height: 180px; background: #fafafa; overflow: hidden; position: relative; }
.card-image { width: 100%; height: 100%; object-fit: cover; }
.card-image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 40px; color: #ddd; }
.card-tag { position: absolute; top: 6px; right: 6px; z-index: 1; }
.card-tag-left { position: absolute; top: 6px; left: 6px; z-index: 1; }
.card-info { padding: 14px; }
.card-title { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.info-item { margin-bottom: 6px; font-size: 12px; color: #666; .label { color: #888; } }
.card-actions { margin-top: 10px; display: flex; gap: 6px; }
.list-header { display: flex; background: #e9e9e9; border-bottom: 1px solid #ddd; font-weight: 500; font-size: 14px; color: #333; }
.header-col { padding: 8px 10px; flex: 1; min-width: 140px; }
.list-content { border: 1px solid #ddd; border-top: none; }
.list-item { display: flex; align-items: center; background: #fff; border-bottom: 1px solid #e5e5e5; cursor: pointer; min-height: 90px; &.gray-row { background: #fafafa; } }
.list-col { padding: 8px 10px; flex: 1; min-width: 140px; }
.list-image { width: 80px; height: 80px; border-radius: 4px; overflow: hidden; background: #f0f0f0; img { width: 100%; height: 100%; object-fit: cover; } }
.empty-state { padding: 60px 0; text-align: center; }
@media (max-width: 1200px) { .grid-view { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 992px) { .grid-view { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .diary-list-container { padding: 15px 10px; } .grid-view { grid-template-columns: 1fr; } .view-switch-top { left: 10px; } .search-wrapper { flex-wrap: wrap; .search-input { width: 100%; } .search-btn, .add-btn { flex: 1; } } }
</style>
