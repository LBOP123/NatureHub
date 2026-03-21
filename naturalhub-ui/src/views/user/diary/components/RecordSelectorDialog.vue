<template>
  <el-dialog
    title="选择观察记录"
    :visible.sync="visible"
    width="70%"
    @close="handleClose"
  >
    <!-- 搜索和筛选 -->
    <div class="selector-toolbar">
      <el-input
        v-model="searchText"
        placeholder="搜索物种名称..."
        prefix-icon="el-icon-search"
        clearable
        style="width: 200px"
      />
      <el-select
        v-model="filterType"
        placeholder="物种类型"
        clearable
        style="width: 150px; margin-left: 10px"
      >
        <el-option
          v-for="item in speciesTypeOptions"
          :key="item.dictValue"
          :label="item.dictLabel"
          :value="item.dictValue"
        />
      </el-select>
      <el-select
        v-model="filterAuditStatus"
        placeholder="审核状态"
        clearable
        style="width: 150px; margin-left: 10px"
      >
        <el-option
          v-for="item in auditStatusOptions"
          :key="item.dictValue"
          :label="item.dictLabel"
          :value="item.dictValue"
        />
      </el-select>
    </div>

    <!-- 记录列表 -->
    <el-table
      ref="table"
      v-loading="loading"
      :data="filteredRecords"
      @selection-change="handleSelectionChange"
      style="width: 100%; margin-top: 15px"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="title" label="标题" width="150" />
      <el-table-column prop="speciesName" label="物种名称" width="120" />
      <el-table-column prop="speciesType" label="类型" width="80">
        <template slot-scope="scope">
          <el-tag :type="getSpeciesTypeTag(scope.row.speciesType)">
            {{ getSpeciesTypeName(scope.row.speciesType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="location" label="地点" width="150" />
      <el-table-column prop="observationTime" label="观察时间" width="150">
        <template slot-scope="scope">
          {{ parseTime(scope.row.observationTime, '{y}-{m}-{d}') }}
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getAuditStatusTag(scope.row.auditStatus)">
            {{ getAuditStatusName(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { listRecord } from '@/api/user/record'
import { getDicts } from '@/api/system/dict/data'

export default {
  name: 'RecordSelectorDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    selectedIds: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      records: [],
      selectedRecords: [],
      searchText: '',
      filterType: null,
      filterAuditStatus: null,
      loading: false,
      speciesTypeOptions: [],
      auditStatusOptions: []
    }
  },
  computed: {
    filteredRecords() {
      return this.records.filter(record => {
        const matchSearch = !this.searchText || 
          record.speciesName.includes(this.searchText) ||
          record.title.includes(this.searchText)
        const matchType = this.filterType === null || record.speciesType == this.filterType
        const matchStatus = this.filterAuditStatus === null || record.auditStatus == this.filterAuditStatus
        return matchSearch && matchType && matchStatus
      })
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.loadDicts()
        this.loadRecords()
      }
    }
  },
  methods: {
    loadDicts() {
      // 加载物种类型字典
      getDicts('nh_species_type').then(res => {
        this.speciesTypeOptions = res.data || []
      }).catch(() => {
        this.speciesTypeOptions = []
      })
      
      // 加载审核状态字典
      getDicts('nh_audit_status').then(res => {
        this.auditStatusOptions = res.data || []
      }).catch(() => {
        this.auditStatusOptions = []
      })
    },
    
    loadRecords() {
      this.loading = true
      listRecord({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.records = res.rows || []
        // 预选已选择的记录
        this.$nextTick(() => {
          if (this.$refs.table) {
            const selectedRows = this.records.filter(r => this.selectedIds.includes(r.recordId))
            this.$refs.table.clearSelection()
            selectedRows.forEach(row => {
              this.$refs.table.toggleRowSelection(row, true)
            })
          }
        })
      }).finally(() => {
        this.loading = false
      })
    },
    
    handleSelectionChange(selection) {
      this.selectedRecords = selection
    },
    
    handleConfirm() {
      const recordIds = this.selectedRecords.map(r => r.recordId)
      this.$emit('confirm', recordIds)
      this.handleClose()
    },
    
    handleClose() {
      this.$emit('update:visible', false)
    },
    
    getSpeciesTypeName(value) {
      const item = this.speciesTypeOptions.find(d => d.dictValue == value)
      return item ? item.dictLabel : '未知'
    },
    
    getSpeciesTypeTag(value) {
      const item = this.speciesTypeOptions.find(d => d.dictValue == value)
      return item ? item.listClass : 'info'
    },
    
    getAuditStatusName(status) {
      const item = this.auditStatusOptions.find(d => d.dictValue == status)
      return item ? item.dictLabel : '未知'
    },
    
    getAuditStatusTag(status) {
      const item = this.auditStatusOptions.find(d => d.dictValue == status)
      return item ? item.listClass : 'info'
    },
    
    parseTime(time, format) {
      return this.$options.filters.parseTime(time, format)
    }
  }
}
</script>

<style scoped>
.selector-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}
</style>
