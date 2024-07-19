<!--管理员-->

<template>
  <div>
    <div class="search">
      <el-input placeholder="Please enter the  file name to search" style="width: 250px" v-model="name"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">query</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">reset</el-button>
    </div>

    <div class="operation">
      <el-button type="danger" plain @click="delBatch">Batch delete</el-button>
    </div>

    <div class="table">
      <el-table :data="tableData" strip @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="id" width="70" align="center" sortable></el-table-column>
        <el-table-column prop="name" label="File name"></el-table-column>
        <el-table-column prop="folder" label="folder"></el-table-column>
        <el-table-column prop="file" label="File path" show-overflow-tooltip></el-table-column>
        <el-table-column prop="userId" label="FounderID"></el-table-column>
        <el-table-column prop="userName" label="Founder"></el-table-column>
        <el-table-column prop="type" label="File type"></el-table-column>
        <el-table-column prop="size" label="File size"></el-table-column>
        <el-table-column prop="crateTime" label="Create time"></el-table-column>
        <el-table-column prop="updateTime" label="Change time"></el-table-column>
        <el-table-column prop="delete" label="Delete" width="100px">
          <template v-slot="scope">
            <span v-if="scope.row.delete">YES</span>
            <span v-else>NO</span>
          </template>
        </el-table-column>

      </el-table>

      <div class="pagination">
        <el-pagination
            background
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total">
        </el-pagination>
      </div>
    </div>



  </div>
</template>

<script>
export default {
  name: "DiskFiles",
  data() {
    return {
      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 6,  // 每页显示的个数
      total: 0,
      name: null,
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      ids: []
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    del(id) {   // 单个删除
      this.$confirm('Are you sure to delete？', 'Confirm to delete', {type: "warning",confirmButtonText: 'Confirm',cancelButtonText: 'Cancel'}).then(response => {
        this.$request.delete('/diskFiles/delete/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('Operation succeeded')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)   //  [1,2]
    },
    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('Please select the file first！')
        return
      }
      this.$confirm('Are you sure to delete these files in bulk?', 'Confirm to delete', {type: "warning",confirmButtonText: 'Confirm',cancelButtonText: 'Cancel'}).then(response => {
        this.$request.delete('/diskFiles/delete/batch', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('Operation succeeded')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/diskFiles/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.tableData = res.data?.list

        this.total = res.data?.total
      })
    },
    reset() {
      this.name = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
  }
}
</script>

<style scoped>

</style>
