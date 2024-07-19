<!--个人信息-->
<template>
  <div class="main-content">
    <el-card style="width: 50%; margin: 30px auto">
      <div style="text-align: right; margin-bottom: 20px">
        <el-button type="primary" @click="updatePassword">Change Password</el-button>
      </div>
      <el-form :model="user" label-width="80px" style="padding-right: 20px">
        <div style="margin: 15px; text-align: center">
          <el-upload
              class="avatar-uploader"
              :action="$baseUrl + '/files/upload'"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
          >
            <img v-if="user.avatar" :src="user.avatar" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </div>
        <el-form-item label="username" prop="username">
          <el-input v-model="user.username" placeholder="Username" disabled></el-input>
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
          <el-button type="primary" @click="update">SAVE</el-button>
        </div>
        <div style="flex: 1; text-align: right">
          <a href="/login">Go back to login</a>
        </div>
      </el-form>
    </el-card>
    <el-dialog title="Change Password" :visible.sync="dialogVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="user" label-width="80px" style="padding-right: 20px" :rules="rules" ref="formRef">
       <el-form-item label="Origin" prop="password">
          <el-input show-password v-model="user.password" placeholder="Original password"></el-input>
        </el-form-item>
        <el-form-item label="New" prop="newPassword">
          <el-input show-password v-model="user.newPassword" placeholder="New password"></el-input>
        </el-form-item>
        <el-form-item label="Confirm" prop="confirmPassword">
          <el-input show-password v-model="user.confirmPassword" placeholder="Confirm password"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">CANCEL</el-button>
        <el-button type="primary" @click="save">CONFIRM</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('Please confirm password'))
      } else if (value !== this.user.newPassword) {
        callback(new Error('Confirm password error'))
      } else {
        callback()
      }
    }
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      dialogVisible: false,

      rules: {
        password: [
          { required: true, message: 'Please enter the original password', trigger: 'blur' },
        ],
        newPassword: [
          { required: true, message: 'Please enter a new password', trigger: 'blur' },
        ],
        confirmPassword: [
          { validator: validatePassword, required: true, trigger: 'blur' },
        ],
      }
    }
  },
  created() {

  },
  methods: {
    update() {
      // 保存当前的用户信息到数据库
      this.$request.put('/user/update', this.user).then(res => {
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
    // 修改密码
    updatePassword() {
      this.dialogVisible = true
    },
    save() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request.put('/updatePassword', this.user).then(res => {
            if (res.code === '200') {
              // 成功更新
              this.$message.success('Password changed successfully! ^w^')
              this.$router.push('/login')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    }
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