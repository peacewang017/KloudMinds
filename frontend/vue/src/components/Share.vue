<template>
  <div>
    <div style="padding: 15px; border-bottom: 1px solid #ddd">
      <el-button type="danger"  class="mycancelsharing" plain @click="delBatch" :disabled="!tableData.length || !ids.length">Cancel sharing</el-button>
    </div>
    <el-table size="medium" :data="tableData" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column label="File name">
        <template v-slot="scope">
          <div style="display: flex; cursor: pointer"  @mouseenter="mouseEnter(scope.row)" @mouseleave="mouseLeave(scope.row)">
            <div style="flex: 1">
              <i style="color: #409EFF" :class="typeList.find(v => v.text === scope.row.type)?.icon || 'el-icon-file'"></i>
              <span v-if="!scope.row.unSave" style="margin-left: 5px">{{ scope.row.name }}</span>
            </div>
            <div style="color: #409EFF; font-size: 14px" v-if="scope.row.optShow">
              <el-tooltip content="cancel the sharing" effect="light" :open-delay="1000">
                <i class="el-icon-remove-outline" style="margin-right: 10px; cursor: pointer" @click="del(scope.row.id)"></i>
              </el-tooltip>
              <el-tooltip content="copy the link" effect="light" :open-delay="1000">
                <i class="el-icon-document-copy" style="margin-right: 10px; cursor: pointer" @click="copy(scope.row)"></i>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="shareTime" label="Sharing time" width="200"></el-table-column>
      <el-table-column prop="status" label="State" width="200">
        <template v-slot="scope">
          <span style="color: red" v-if="scope.row.status.includes('小时')">{{ scope.row.status }}</span>
          <span v-else>{{ scope.row.status }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="count" label="visits" width="200"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: "ShareComponent",
  props: {
    typeList: null
  },
  data() {
    return {
      tableData: [],
      ids: []
    }
  },
  created() {
    this.load()
  },
  methods: {
    copy(row) {
      let currentUrl = location.href.substring(0, location.href.indexOf('/front'))
      let shareLink = currentUrl + '/front/openShare?code=' + row.code + '&shareId=' + row.id

      let _input = document.createElement("input");   // 直接构建input
      _input.value = shareLink;  // 设置内容
      document.body.appendChild(_input);    // 添加临时实例
      _input.select();   // 选择实例内容
      document.execCommand("Copy");   // 执行复制
      document.body.removeChild(_input)
      this.$message.success("复制成功")
    },
    del(id) {   // 单个删除
      this.$confirm('Are you sure to cancel the sharing?', 'Confirm to cancel the sharing', {type: "warning",confirmButtonText: 'Confirm',cancelButtonText: 'Cancel',}).then(response => {
        this.$request.delete('/share/delete/' + id).then(res => {  // 这里传的是 trash的id  要传file_id
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('Operation successful!')
            this.load()
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
      this.$confirm('Are you sure to cancel the sharing?', 'Confirm to cancel the sharing', {type: "warning",confirmButtonText: 'Confirm',cancelButtonText: 'Cancel'}).then(response => {
        this.$request.delete('/share/delete/batch', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('Operation successful!')
            this.load()
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    load() {
      this.$request.get('/share/selectAll').then(res => {
        this.tableData = res.data || []
      })
    },
    mouseEnter(row) {
      this.$set(row, 'optShow', true)
    },
    mouseLeave(row) {
      this.$set(row, 'optShow', false)
    }
  }
}
</script>

<style scoped>
.mycancelsharing {
  background-color: #fedb9c; /* 背景颜色 */
  color: #fc8811; /* 文字颜色 */
  border: 1px solid #fc8811; /* 添加黑色边框，边框宽度为1px */
  border-radius: 30px; /* 圆角大小 */
  padding: 10px 17px; /* 减小内边距 */
  font-size: 12px; /* 减小字体大小 */
  transition: all 0.3s; /* 过渡效果 */
  margin-left: 20px; /* 向右移动20px */
}

/*上传文件悬停样式*/
.mycancelsharing:hover {
  background-color:#fc8811; /* 鼠标悬停时的背景颜色，保持不变 */
  color: #fedb9c; /* 鼠标悬停时的文字颜色，保持不变 */
  opacity: 0.9; /* 鼠标悬停时的透明度，增加层次感 */
}
</style>