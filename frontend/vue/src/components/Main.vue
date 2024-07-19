<template>
  <div v-if="category === 'all' || category === 'img' || category === 'video'|| category === 'zip'" >
    <div style="padding: 15px; border-bottom: 1px solid #ddd;position: relative">
      <el-upload
          style="display: inline-block; margin-right: 10px"
          :action="uploadUrl"
          :headers="{ token: user.token }"
          :show-file-list="false"
          :on-success="handleFileSuccess"
      >
        <el-button class="myupload">Upload files</el-button>
      </el-upload>
      <el-button type="primary" plain @click="addFolder" class="mynewfold">New folder</el-button>
      <el-button type="danger" plain @click="delBatch"  class="mydel">Batch delete</el-button>
      <el-input class="search"
                v-model="str1"
                placeholder="Please enter to search"
                style="margin-left: 50px; width: 200px;"
                :prefix-icon="'el-icon-search'"
                @mouseenter="iconHover = true"
                @mouseleave="iconHover = false">
      </el-input>
      <el-button-group style="margin-left: 10px">
        <el-button type="info"  class="mysearch"  @click="search(1)">Exact</el-button>
        <el-button type="info"  class="mysearch" @click="vaguesearch(1)">Vague</el-button>
      </el-button-group>
      <!--<el-button type="info" plain style="margin-left: 10px" @click="search(1)" round>
        <i class="el-icon-right"></i>
      </el-button>-->
      <el-button type="warning" plain style="margin-left: 10px" @click="reset" round>
        <i class="el-icon-refresh-right"></i>
      </el-button>
      <el-dropdown split-button type="primary"  style="margin-left:60px" @command="handleCommand" v-if="category === 'all' || category === 'img' || category === 'video'|| category === 'zip'">
        AI functionality
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="1">AI Summary</el-dropdown-item>
          <el-dropdown-item command="2">AI Chat</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <div style="padding: 15px">
      <!-- 进度条组件，设置为25%，根据百分比区间设置颜色 -->
      <span class="progress-label" style="font-size: 15px; font-family: 'Helvetica Neue', sans-serif; color:#4a77a2;margin-bottom: 6px;">Cloud storage space</span>
      <el-button
          type="primary"
          style="margin-left: 700px;border-radius: 10px;transform: scale(0.85);margin-bottom: 7px"
          class="custom-button"
          icon="el-icon-rank"
          @click="handleExpand">Expand capacity</el-button>
      <el-progress :percentage="percentage" :format="format" :color="getColor" style="width: 100%; position: relative; left: 5px;"></el-progress>
    </div>

    <div style="flex: 1; padding: 0 20px; overflow: hidden;">
      <div style="height: 500px; overflow-y: auto;">
      <el-table size="medium" stripe :data="tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column label="File Name">
          <template v-slot="scope">
            <div v-if="scope.row.unSave" >
              <i style="color: #409EFF" :class="typeList.find(v => v.text === scope.row.type)?.icon || 'el-icon-file'"></i>
              <el-input size="mini" style="width: 300px; margin: 0 5px" v-model="scope.row.name"></el-input>
              <el-button type="primary" size="mini" @click="save(scope.row)">determine</el-button>
              <el-button type="danger" size="mini" @click="cancel(scope.row)">cancel</el-button>
            </div>

            <div style="display: flex; cursor: pointer" @click="openFile(scope.row)" v-else @mouseenter="mouseEnter(scope.row)" @mouseleave="mouseLeave(scope.row)">
              <div style="flex: 1">
                <i style="color: #409EFF" :class="typeList.find(v => v.text === scope.row.type)?.icon || 'el-icon-file'"></i>
                <span v-if="!scope.row.unSave" style="margin-left: 5px">{{ scope.row.name }}</span>
              </div>
              <div style="color: #409EFF; font-size: 14px" v-if="scope.row.optShow">
                <el-tooltip content="Share" effect="light" :open-delay="1000">
                  <i class="el-icon-share" style="margin-right: 10px; cursor: pointer" @click.stop="share(scope.row)"></i>
                </el-tooltip>
                <el-tooltip content="Download" effect="light" :open-delay="1000" v-if="scope.row.folder === '否'">
                  <i class="el-icon-download" style="margin-right: 10px; cursor: pointer" @click.stop="download(scope.row)"></i>
                </el-tooltip>
                <el-tooltip content="Delete" effect="light" :open-delay="1000">
                  <i class="el-icon-delete" style="margin-right: 10px; cursor: pointer" @click.stop="del(scope.row.id)"></i>
                </el-tooltip>
                <el-tooltip content="Rename" effect="light" :open-delay="1000">
                  <i class="el-icon-rename" style="margin-right: 10px; cursor: pointer" @click.stop="rename(scope.row)"></i>
                </el-tooltip>
                <el-tooltip content="Copy" effect="light" :open-delay="1000">
                  <i class="el-icon-document-copy" style="cursor: pointer" @click.stop="copy(scope.row)"></i>
                </el-tooltip>
              </div>

            </div>
          </template>
        </el-table-column>

        <el-table-column prop="updateTime" label="Update Time" width="300"></el-table-column>
        <el-table-column prop="size" label="File Size（KB）" width="300"></el-table-column>
      </el-table>
      </div>

    </div>

    <el-dialog title="SHARE" :visible.sync="shareVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form label-width="150px" style="padding-right: 50px" :model="form">
        <el-form-item label="Sharing Days">
          <el-radio-group v-model="form.days">
            <el-radio :label="3">3 days</el-radio>
            <el-radio :label="7">7 days</el-radio>
            <el-radio :label="30">30 days</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Share link" v-if="shareLink">
          <div style="display: flex">
            <el-input style="flex: 1" v-model="shareLink"></el-input>
            <el-button style="margin-left: 10px" type="primary" @click="copyLink(shareLink)">Copy it!</el-button>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="shareVisible = false">CANCEL</el-button>
        <el-button type="primary" @click="saveShare">CONFIRM</el-button>
      </div>
    </el-dialog>

    <el-dialog title=" Please choose the storage you want " :visible.sync="expandDialogVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form label-width="150px" style="padding-right: 50px" :model="expandForm">
        <el-form-item label="Storage Space">
          <el-radio-group v-model="expandForm.storage">
            <el-radio label="200G">200 G</el-radio>
            <el-radio label="500G">500 G</el-radio>
            <el-radio label="1T">1 T</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="expandDialogVisible = false" class="myupload">CANCEL</el-button>
        <el-button type="primary" @click="handleSubmitExpand" class="mynewfold">CONFIRM</el-button>
      </div>
    </el-dialog>

    <el-dialog
        :title="title"
        :visible.sync="resultDialog"
        width="50%">
      <span style="font-size: 20px;">{{aiResult}}</span>
    </el-dialog>
    <!--弹出输入问题-->
    <div style="background-color: #709bc8;">
    <el-dialog
        title="AI Chat"
        :visible.sync="msgDialog"
        :destroy-on-close="true"
        width="50%"
        style="font-size: 20px; font-weight: 400; font-family: 'Proxima Nova', sans-serif;"
        class="wrapper">
      <div style="box-shadow: 0 0 20px 0 #709bc8;" >
        <el-form :model="msgForm" ref="ruleForm" class="demo-ruleForm">
          <el-form-item :rules="[{ required: true, message: 'Please enter the question', trigger: 'blur' }]" prop="msg">
            <el-input
                type="textarea"
                :rows="5"
                placeholder="Please enter your question"
                v-model="msgForm.msg">
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="msgDialog = false" class ="myupload">CANCEL</el-button>
        <el-button type="primary" @click="handleSubmit" class="mynewfold">CONFIRM</el-button>
      </span>
    </el-dialog>
    </div>

  </div>
</template>

<script>
export default {
  name: "Main",
  props: {
    typeList: null
  },
  data() {
    return {
      msgForm:{
        msg:null
      },
      expandForm:{
        storage: '200G'
      },
      expandDialogVisible:false,
      title:'notice',
      msgDialog:false,
      category: this.$route.query.category,  // 分类
      folderId: this.$route.query.folderId,  // folderId
      tableData: [],
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      uploadUrl: '',
      ids: [],
      resultDialog:false,
      folders: [],
      currentRows:[],
      aiResult:null,
      shareVisible: false,
      form: {},
      shareLink: '',
      name: null,
      str1:null,
      aiOptions: 1, // AI功能选项
      aiFormVisible: false, // 控制AI功能表单的显示
      iconHover: false, // 用于控制图标放大的状态
      percentage: 0, // 进度条初始值
      maxCapacity:0,
      usedCapacity:0,
    }
  },
  created() {
    this.load()
    this.uploadUrl = this.$baseUrl + '/diskFiles/add?folder=否'
    if (this.folderId) {  // 如果不这么判断  就会传 undefined到后台 就会报错
      this.uploadUrl += '&folderId=' + this.folderId
    }
    this.getExpand();
  },
  methods: {
    getExpand(){
      this.$request.get(`/user/selectCapacityById`).then(res=>{
        this.usedCapacity = res.data.usedCapacity / 1024 / 1024
        this.maxCapacity = res.data.maxCapacity / 1024 / 1024
        this.percentage = res.data.usedCapacity / res.data.maxCapacity
      })
    },
    format(val){
      return `${this.usedCapacity.toFixed(6)}G/${this.maxCapacity}G`
    },
    handleSubmitExpand(){
      let temp;
      if(this.expandForm.storage.includes('T')){
        // T
        temp = 1024 * 1024 * 1024 * parseFloat(this.expandForm.storage)
      }else{
        // G
        temp = 1024 * 1024 * parseFloat(this.expandForm.storage)
      }
      this.$request.put(`/user/updateCapacity/${temp}`).then(res=>{
        if(res.code === "200"){
          this.getExpand();
          this.$message.success('Expansion successful！')
          this.expandDialogVisible = false;
        }
      })
    },
    handleExpand(){
      this.expandForm.storage = '200G'
     this.expandDialogVisible = true;
    },
    saveShare() {
      if (this.shareLink) {
        this.shareVisible = false
        return
      }
      this.$request.post('/diskFiles/share/', this.form).then(res => {
        if (res.code === '200') {
          this.$message.success("Operation succeeded")

          let shareData = res.data
          let currentUrl = location.href.substring(0, location.href.indexOf('/front'))
          this.shareLink = currentUrl + '/front/openShare?code=' + shareData.code + '&shareId=' + shareData.id
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleCommand(val) {
      if(this.currentRows.length === 0){
        this.$message.error('Please choose the document first!')
        return;
      }
      if(val === '1'){
        this.title = 'Document Summary'
        let temp = this.currentRows.map(item=>item.name).join(',')
        this.$request.get(`/aiChat/doc/${temp}`).then(res=>{
          this.aiResult = res;
          this.resultDialog = true;
        })
      }else if( val === '2'){
        this.msgForm.msg=null;
        this.msgDialog = true;
      }
    },
    handleSubmit(){
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          this.msgDialog = false;
          this.title = this.msgForm.msg;
          let temp = this.currentRows.map(item=>item.name).join(',')
          this.$request.get(`/aiChat/chat/${temp}?msg=${this.msgForm.msg}`).then(res=>{
            this.aiResult = res;
            this.msgForm.msg = null;
            this.resultDialog = true;
          })
        } else {
          console.log('error submit!!');
          return false;
        }
      });

    },
    share(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.form.days = 3
      this.shareLink = ''
      this.shareVisible = true
    },
    copy(row) {
      this.$request.post('/diskFiles/copy/' + row.id).then(res => {
        if (res.code === '200') {
          this.$message.success("Operation succeeded")
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    openFile(row) {
      if (row.folder === '是') { // 文件夹
        location.href = '/front/home?category=' + this.category + '&folderId=' + row.id
      } else {
        this.$request.get('/diskFiles/preview/' + row.name).then(res => {
          window.open(res)
        })
      }
    },
    search(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/diskFiles/selectByStr', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          str: this.str1,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    vaguesearch(pagenum) {
      if (pagenum) this.pageNum = pagenum
      this.$request.get('/diskFiles/selectByStrLike', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          str: this.str1,
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
    download(row) {
      //如果后端改好的话
      this.$request.get('/diskFiles/download/' + row.name).then(res => {
        window.open(res)
      })
    },
    mouseEnter(row) {
      if (!row.unSave) {
        this.$set(row, 'optShow', true)
      }
    },
    mouseLeave(row) {
      if (!row.unSave) {
        this.$set(row, 'optShow', false)
      }
    },
    rename(row) {
      this.$set(row, 'unSave', true)
    },
    save(row) {  // 上传文件夹的方法 和编辑后保存
      if (row.id) {  // 编辑
        this.$request.put('/diskFiles/update', row).then(res => {
          if (res.code === '200') {
            this.$message.success("Operation succeeded")
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      } else {
        let url = '/diskFiles/add?folder=是&name=' + row.name
        if (this.folderId) {  // 外层的folderId
          url += '&folderId=' + this.folderId
        }
        this.$request.post(url).then(res => {
          if (res.code === '200') {
            this.$message.success("Operation succeeded")
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      }

    },
    cancel(row) {
      let index = row.$index
      if (row.id) {  // 编辑
        this.load() // 加载最新的数据即可
      } else {  // 新增
        this.tableData.splice(index, 1)
      }

    },
    addFolder() {
      this.tableData.unshift({  name: '', type: 'folder', unSave: true, folderId: this.folderId || null })    //  unSave 控制输入框是否显示
    },
    load() {
      this.$request.get('/diskFiles/selectAll', {
        params: {
          category: this.category || null,   // all  img  video  zip
          folderId: this.folderId || null
        }
      }).then(res => {
        this.tableData = res.data || []
        this.tableData.forEach(item => {
          this.$set(item, 'optShow', false)   // 强制设置每行  加一个 optShow属性
        })
      })

      this.$request.get('/diskFiles/selectFolders', {
        params: { folderId: this.folderId || null }
      }).then(res => {
        this.folders = res.data || []
      })
    },
    handleFileSuccess(res) {
      if (res.code === '200') {
        this.$message.success("File uploaded successfully  ^w^")
        this.load()
      } else {
        this.$message.error('File upload failed  QwQ')
      }
    },
    del(id) {   // 单个删除
      this.$confirm('Are you sure to delete？', 'Confirm to delete', {type: "warning",confirmButtonText: 'Confirm',cancelButtonText: 'Cancel'}).then(response => {
        this.$request.delete('/diskFiles/trash/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('Operation succeeded')
            this.load()
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.currentRows  = rows;
      this.ids = rows.map(v => v.id)   //  [1,2]
    },
    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('Please select the file first！')
        return
      }
      this.$confirm('Are you sure to delete these files in bulk?', 'Confirm to delete', {type: "warning",confirmButtonText: 'Confirm',cancelButtonText: 'Cancel'}).then(response => {
        this.$request.delete('/diskFiles/trash/batch', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('Operation succeeded')
            this.load()
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    copyLink(link) {
      let _input = document.createElement("input");   // 直接构建input
      _input.value = link;  // 设置内容
      document.body.appendChild(_input);    // 添加临时实例
      _input.select();   // 选择实例内容
      document.execCommand("Copy");   // 执行复制
      document.body.removeChild(_input)
      this.$message.success("Copy success")
    },
    //进度条颜色
    getColor() {
      if (this.percentage < 30) {
        return '#67c23a'; // 绿色
      } else if (this.percentage >= 30 && this.percentage < 60) {
        return '#e6a23c'; // 黄色
      } else if (this.percentage >= 60 && this.percentage < 90) {
        return '#f56c6c'; // 橙色
      } else {
        return '#f00'; // 红色
      }
    },
  }
}
</script>

<style scoped>
/deep/ .el-dialog{
  background-color: #EBEDDF;
}
.el-dropdown {
  vertical-align: top;
}
.el-dropdown + .el-dropdown {
  margin-left: 15px;
}
.el-icon-arrow-down {
  font-size: 12px;
}
.myupload {
  background-color: #EBEDDF; /* 背景颜色 */
  color: #709bc8; /* 文字颜色 */
  border: 1px solid #709bc8; /* 添加边框，边框宽度为1px */
  border-radius: 4px; /* 圆角大小 */
  padding: 10px 17px; /* 减小内边距 */
  font-size: 12px; /* 减小字体大小 */
  transition: all 0.3s; /* 过渡效果 */
}

/*上传文件悬停样式*/
.myupload:hover {
  background-color: #709bc8; /* 鼠标悬停时的背景颜色，保持不变 */
  color: #EBEDDF; /* 鼠标悬停时的文字颜色，保持不变 */
  opacity: 0.9; /* 鼠标悬停时的透明度，增加层次感 */
}
.mynewfold {
  background-color: #709bc8; /* 背景颜色 */
  color: #EBEDDF; /* 文字颜色 */
  border: 1px solid #EBEDDF; /* 添加黑色边框，边框宽度为1px */
  border-radius: 4px; /* 圆角大小 */
  padding: 10px 17px; /* 减小内边距 */
  font-size: 12px; /* 减小字体大小 */
  transition: all 0.3s; /* 过渡效果 */
  margin-left: 10px; /* 向右移动20px */
}

/*上传文件悬停样式*/
.mynewfold:hover {
  background-color: #EBEDDF; /* 鼠标悬停时的背景颜色，保持不变 */
  color: #709bc8; /* 鼠标悬停时的文字颜色，保持不变 */
  opacity: 0.9; /* 鼠标悬停时的透明度，增加层次感 */
}
.mydel {
  background-color: #fedb9c; /* 背景颜色 */
  color: #fc8811; /* 文字颜色 */
  border: 1px solid #fc8811; /* 添加黑色边框，边框宽度为1px */
  border-radius: 30px; /* 圆角大小 */
  padding: 10px 17px; /* 减小内边距 */
  font-size: 12px; /* 减小字体大小 */
  transition: all 0.3s; /* 过渡效果 */
  margin-left: 20px; /* 向右移动20px */
}

.mydel:hover {
  background-color:#fc8811; /* 鼠标悬停时的背景颜色，保持不变 */
  color: #fedb9c; /* 鼠标悬停时的文字颜色，保持不变 */
  opacity: 0.9; /* 鼠标悬停时的透明度，增加层次感 */
}
.el-input__icon.el-icon-search {
  transition: transform 0.3s; /* 平滑过渡效果 */
  transform: scale(1); /* 初始大小 */
}

/* 鼠标悬停时图标放大 */
.el-input__icon.el-icon-search:hover {
  transform: scale(1.3); /* 鼠标悬停时放大到130% */
}

/* 设置下拉菜单的样式 */
.el-dropdown-menu {
  background-color: #EBEDDF; /* 底色 */
  color: #EBEDDF; /* 文字颜色 */
}

/* 设置下拉菜单项的悬停效果 */
.el-dropdown-menu__item:hover {
  background-color: #709bc8; /* 鼠标悬停时的底色 */
  color: #EBEDDF; /* 鼠标悬停时的文字颜色 */
}
.el-table--striped .el-table__body .el-table__row:nth-child(odd) {
  background-color: #add8e6; /* 浅蓝色 */
}
.el-table--striped .el-table__body .el-table__row:nth-child(even) {
  background-color: #ffe4e1; /* 浅粉色 */
}

/deep/ .el-progress-bar{
  width:95% !important;
 }
.custom-button {
  background-color: #cfecfe; /* 浅蓝色背景 */
  border: 1px solid #52aafe; /* 深蓝色边框 */
  color: #52aafe; /* 深蓝色文字 */
}

/* 鼠标悬停时的样式 */
.custom-button:hover {
  background-color: #52aafe; /* 背景变为深蓝色 */
  color: #cfecfe; /* 字体变为浅蓝色 */
}
.mysearch {
  background-color: #feeed2; /* 背景颜色 */
  color: #8fa8d7; /* 文字颜色 */
  border: 1px solid #8fa8d7; /* 添加边框，边框宽度为1px */
  border-radius: 20px; /* 圆角大小 */
  padding: 10px 17px; /* 减小内边距 */
  font-size: 12px; /* 减小字体大小 */
  transition: all 0.3s; /* 过渡效果 */
  height: calc(100% - 15px);
}

/*上传文件悬停样式*/
.mysearch:hover {
  background-color: #8fa8d7; /* 鼠标悬停时的背景颜色，保持不变 */
  color: #feeed2; /* 鼠标悬停时的文字颜色，保持不变 */
  opacity: 0.9; /* 鼠标悬停时的透明度，增加层次感 */
}
</style>