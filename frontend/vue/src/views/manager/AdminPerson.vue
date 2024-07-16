<!--管理员个人信息-->
<template>
  <div style="display: flex; justify-content: center; align-items: start; height: 100vh;margin-bottom: 500px">
    <el-card style="width: 50%;margin-top: 50px;background-color: #ebf5ff;">
      <el-form :model="user" label-width="100px" style="padding-right: 50px">
        <div style="margin: 15px; text-align: center">
          <el-upload
              class="avatar-uploader"
              :action="$baseUrl + '/files/upload'"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              style="margin-left: 60px;"
          >
            <img v-if="user.avatar" :src="user.avatar" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </div>
        <el-form-item label="username" prop="username">
          <el-input v-model="user.username" placeholder="username" disabled></el-input>
        </el-form-item>
        <el-form-item label="name" prop="name">
          <el-input v-model="user.name" placeholder="Name"></el-input>
        </el-form-item>
        <el-form-item label="phone" prop="phone">
          <el-input v-model="user.phone" placeholder="Phone"></el-input>
        </el-form-item>
        <el-form-item label="email" prop="email">
          <el-input v-model="user.email" placeholder="Email"></el-input>
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="primary" @click="update" style="margin-left: 60px;">SAVE</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "AdminPerson",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}')
    }
  },
  created() {

  },
  methods: {
    update() {
      // 保存当前的用户信息到数据库
      this.$request.put('/admin/update', this.user).then(res => {
        if (res.code === '200') {
          // 成功更新
          this.$message.success('Saved successfully!')

          // 更新浏览器缓存里的用户信息
          localStorage.setItem('xm-user', JSON.stringify(this.user))

          // 触发父级的数据更新
          this.$emit('update:user')
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleAvatarSuccess(response, file, fileList) {
      // 把user的头像属性换成上传的图片的链接
      this.$set(this.user, 'avatar', response.data)
    },
  }
}
</script>

<style scoped>
/deep/.el-form-item__label {
  font-weight: bold;
}
/deep/.el-upload {
  border-radius: 50%;
}
/deep/.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border-radius: 50%;
}
/deep/.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  border-radius: 50%;
}
.avatar {
  width: 120px;
  height: 120px;
  display: block;
  border-radius: 50%;
}
</style>