<template>
  <div style="display: flex; justify-content: center; align-items: start; height: 100vh;">
    <el-card style="width: 50%;margin-top: 100px;background-color: #ebf5ff;">
      <el-form ref="formRef" :model="user" :rules="rules" label-width="100px" style="padding-right: 50px">
        <el-form-item label="Origin" prop="password">
          <el-input show-password v-model="user.password" placeholder="Original password"></el-input>
        </el-form-item>
        <el-form-item label="New" prop="newPassword">
          <el-input show-password v-model="user.newPassword" placeholder="New word"></el-input>
        </el-form-item>
        <el-form-item label="New" prop="confirmPassword">
          <el-input show-password v-model="user.confirmPassword" placeholder="Confirm new password"></el-input>
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="primary" @click="update">SAVE</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Password",
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
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request.put('/updatePassword', this.user).then(res => {
            if (res.code === '200') {
              // 成功更新
              localStorage.removeItem('xm-user')   // 清除缓存的用户信息
              this.$message.success('Password changed successfully！ ^w^')
              this.$router.push('/login')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    },
  }
}
</script>

<style scoped>
/deep/.el-form-item__label {
  font-weight: bold;
}
</style>