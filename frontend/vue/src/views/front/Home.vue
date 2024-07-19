<template>
  <div style="background-color: white; display: flex; height: 100vh;">
    <div style="display: flex; width: 100%;">
      <el-col :span="4" class="sidebar" style="min-height: 100vh; background-color: #709bc8; border-right: 1px solid #ddd;">
        <el-menu
            default-active="null"
            class="el-menu-vertical-demo"
            @open="handleOpen"
            @close="handleClose"
            @select="handleSelect"
            background-color="#709bc8"
            text-color="#EBEDDF"
            active-text-color="#ffd04b"
            style="border-right: none;">
          <!-- 全部文件的下拉列表 -->
          <el-submenu index="all">
            <template slot="title">
              <i class="el-icon-menu" style="color:#df9e2d"></i>
              <span style="font-size: 14px; font-weight: 500; font-family: 'Proxima Nova', sans-serif;">Document</span>
            </template>
            <!-- 下拉列表的选项 -->
            <el-menu-item index="all">
              <i class="el-icon-document" style="color:#fec740;"></i>
              <span style="font-size: 13px; font-weight: 400; font-family: 'Proxima Nova', sans-serif;"> All Files</span>
            </el-menu-item>
            <el-menu-item index="img">
              <i class="el-icon-picture-outline" style="color:#fec740"></i>
              <span style="font-size: 13px; font-weight: 400; font-family: 'Proxima Nova', sans-serif;">Pictures</span>
            </el-menu-item>
            <el-menu-item index="video">
              <i class="el-icon-video-play" style="color:#fec740"></i>
              <span style="font-size: 13px; font-weight: 400; font-family: 'Proxima Nova', sans-serif;">Videos</span>
            </el-menu-item>
            <el-menu-item index="zip">
              <i class="el-icon-box" style="color:#fec740"></i>
              <span style="font-size: 13px; font-weight: 400; font-family: 'Proxima Nova', sans-serif;">Compressed Files</span>
            </el-menu-item>
          </el-submenu>
          <!-- 我的分享 -->
          <el-menu-item index="share">
            <i class="el-icon-share" style="color:#df9e2d"></i>
            <span slot="title" style="font-size: 14px; font-weight: 500; font-family: 'Proxima Nova', sans-serif;">Share</span>
          </el-menu-item>
          <!-- 回收站 -->
          <el-menu-item index="trash">
            <i class="el-icon-delete" style="color:#df9e2d"></i>
            <span slot="title" style="font-size: 14px; font-weight: 500; font-family: 'Proxima Nova', sans-serif;">Recycle Bin</span>
          </el-menu-item>
        </el-menu>
      </el-col>
      <div style="flex: 1; padding: 0 20px; background-color: #fdf5e6;">
        <!-- 根据 category 显示不同的组件 -->
        <component :is="currentComponent" :type-list="typeList" v-if="currentComponent"></component>
      </div>
    </div>
  </div>
</template>

<script>
import Main from "@/components/Main";
import Trash from "@/components/Trash";
import Share from "@/components/Share";

export default {
  components: {
    Main,
    Trash,
    Share
  },
  data() {
    return {
      category: this.$route.query.category || 'all',  // 默认分类为全部文件
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
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
    };
  },
  computed: {
    currentComponent() {
      const componentMap = {
        'share': Share,
        'trash': Trash,
        'all': Main,
        'img': Main,
        'video': Main,
        'zip': Main,
      };
      return componentMap[this.category] || Main;
    }
  },
  methods: {
    handleOpen() {},
    handleClose() {},
    handleSelect(key, keyPath) {
      this.category = key;
      this.loadFiles(key);
    },
    loadFiles(category) {
      location.href = '/front/home?category=' + category;
    }
  }
}
</script>

<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 100%;
}
.category-active {
  color: #409EFF;
}
.sidebar {
  width: 17%;
}
</style>
