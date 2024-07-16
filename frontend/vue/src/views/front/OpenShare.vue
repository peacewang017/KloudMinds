<template>
  <div>
    <div style="margin: 150px auto; text-align: center; font-size: 30px; color: #666" v-if="validateFailed">分享文件不存在或已被取消</div>
    <div class="card" style="margin: 5px auto 100px auto; width: 70%" v-else>
      <div style="color: #666; display: flex; align-items: center">
        分享：<strong style="color: #333">{{ share.name }}</strong>
        <i class="el-icon-time" style="margin-left: 20px; margin-right: 5px"></i> {{ share.shareTime }}
        <span style="margin-left: 20px">
          <i class="el-icon-info" style="margin-right: 5px"></i>
          <span v-if="share.status?.includes('小时')" style="color: red">{{ share.status }}</span>
          <span v-else>{{ share.status }}</span>
        </span>
      </div>

      <div>
        <div style="padding: 15px 0; margin-top: 30px">
          <a style="color: #666" :href="'/front/openShare?code=' + code + '&shareId=' + shareId">全部文件 <i class="el-icon-arrow-right" v-if="folders.length"></i></a>
          <a style="color: #666" :href="'/front/openShare?code=' + code + '&shareId=' + shareId + '&folderId=' + item.id" v-for="(item, index) in folders" :key="item.id">
            {{ item.name }} <i class="el-icon-arrow-right" v-if="index !== folders.length - 1"></i>
          </a>
        </div>
        <el-table size="medium" stripe :data="tableData">
          <el-table-column label="名称">
            <template v-slot="scope">
              <div style="display: flex; cursor: pointer" @click="openFile(scope.row)">
                <div style="flex: 1">
                  <i style="color: #409EFF" :class="typeList.find(v => v.text === scope.row.type)?.icon || 'el-icon-file'"></i>
                  <span v-if="!scope.row.unSave" style="margin-left: 5px">{{ scope.row.name }}</span>
                </div>
                <div style="color: #409EFF; font-size: 14px">
                  <el-tooltip content="下载" effect="light" :open-delay="1000" v-if="scope.row.folder === '否'">
                    <i class="el-icon-download" style="margin-right: 10px; cursor: pointer" @click.stop="download(scope.row.file)"></i>
                  </el-tooltip>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="updateTime" label="修改时间" width="200"></el-table-column>
          <el-table-column prop="size" label="文件大小（KB）" width="200"></el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "OpenShare",
  data() {
    return {
      code: this.$route.query.code,
      shareId: this.$route.query.shareId,
      share: {},
      validateFailed: false,   // 验证失败
      folders: [],
      typeList: [
        { text: 'mp3', icon: 'el-icon-mp3' },
        { text: 'mp4', icon: 'el-icon-mp4' },
        { text: 'jpg', icon: 'el-icon-jpg' },
        { text: 'jpeg', icon: 'el-icon-jpeg' },
        { text: 'png', icon: 'el-icon-png' },
        { text: 'pdf', icon: 'el-icon-pdf' },
        { text: 'docx', icon: 'el-icon-docx' },
        { text: 'txt', icon: 'el-icon-text' },
        { text: 'zip', icon: 'el-icon-zip' },
        { text: 'folder', icon: 'el-icon-folder' },
      ],
      tableData: [],
      folderId: this.$route.query.folderId   // 注意给 全局的folderId 赋值，要不然后面没法查询数据
    }
  },
  created() {
    if (!this.code || !this.shareId) {
      this.validateFailed = true  // 缺少必须的参数
      return
    }

    this.load()
  },
  methods: {
    openFile(row) {
      if (row.folder === '是') { // 文件夹  点进来肯定有 folderId
        location.href = '/front/openShare?code=' + this.code + '&shareId=' + this.shareId + '&folderId=' + row.id
      } else {  // 如果是文件的话  直接预览
        window.open(this.$baseUrl + '/diskFiles/preview/' + row.id)
      }
    },
    download(url) {
      window.open(url)  // 文件下载
    },
    load() {
      this.$request.get('/share/selectById/' + this.shareId).then(res=> {
        this.share = res.data || {}
        this.tableData = res.data ? [res.data] : []
        if (this.share.status === '已过期') {
          this.validateFailed = true
        }

        // 验证code对不对
        if (this.share.code !== this.code) {
          this.validateFailed = true
        } else {
          this.loadFiles()
        }
      })
    },
    loadFiles() {
      // 查询当前目录的数据
      this.$request.get('/diskFiles/selectShare', {
        params: {
          shareId: this.shareId || null,
          folderId: this.folderId || null
        }
      }).then(res => {
        this.tableData = res.data || []
      })

      // 查询当前目录的路径数据
      this.$request.get('/diskFiles/selectFolders', {
        params: { folderId: this.folderId || null }
      }).then(res => {
        this.folders = res.data || []
      })
    }
  }
}
</script>

<style scoped>

</style>