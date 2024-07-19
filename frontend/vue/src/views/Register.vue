<template>
  <div class="container">
    <div style="height: 60px; position: fixed; top: 0; display: flex; align-items: center; padding-left: 20px">
      <img src="@/assets/imgs/logo.png" alt="" style="width: 50px;transform: scale(1.5);margin-top: 15px">
      <span style="font-family: 'Futura PT', sans-serif;color: #feac2c; font-size: 24px; font-weight: bold; margin-left: 65px;margin-top: 15px;transform: scale(1.5); text-shadow: 1px 2px 5px black;">KloudMinds</span>
    </div>

    <div style="flex: 1; display: flex; justify-content: center">
      <div style="width: 350px; padding: 50px 30px; box-shadow: 0 0 10px #667db3; background-color: #feefc7; border-radius: 5px;">
        <div style="text-align: center; font-size: 24px; font-weight: bold; margin-bottom: 30px; color:black">WELCOME HERE</div>
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="username">
            <el-input size="medium" prefix-icon="el-icon-user" placeholder="Please enter your username" v-model="form.username"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input size="medium" prefix-icon="el-icon-lock" placeholder="Please input your password" show-password  v-model="form.password"></el-input>
          </el-form-item>
          <el-form-item prop="confirmPass">
            <el-input size="medium" prefix-icon="el-icon-lock" placeholder="Please confirm password" show-password  v-model="form.confirmPass"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button style="width: 100%; background-color: #2a60c9; border-color: #2a60c9; color: white" @click="register">Register</el-button>
          </el-form-item>
          <div style="display: flex; align-items: center">
            <div style="flex: 1"></div>
            <div style="display: flex; flex-direction: column; align-items: center; text-align: center; flex-wrap: wrap;margin-right: 55px">
              Already have an account??
              <div style="display: inline-flex; align-items: center;">
                Please&nbsp<a href="/login"> login</a>
              </div>
            </div>
          </div>
        </el-form>
      </div>
    </div>
    <div style="flex: 1; display: flex; justify-content: center">
      <img src="@/assets/imgs/register.svg" alt="" style="width: 80%">
    </div>
  </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    // 验证码校验
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('Please confirm password'))
      } else if (confirmPass !== this.form.password) {
        callback(new Error('Entered passwords differ!'))
      } else {
        callback()
      }
    }
    return {
      form: { role: 'USER' },
      rules: {
        username: [
          { required: true, message: 'Please enter your username', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'Please input your password', trigger: 'blur' },
        ],
        confirmPass: [
          { validator: validatePassword, trigger: 'blur' }
        ]
      }
    }
  },
  created() {

  },
  methods: {
    register() {
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          this.$request.post('/register', this.form).then(res => {
            if (res.code === '200') {
              this.$router.push('/login')  // 跳转登录页面
              this.$message.success('Register successful')
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
.container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  background: linear-gradient(to bottom left, #afc0e0 250px, transparent 100px),
  linear-gradient(to top left, #fed89a, transparent);
}

.image-container img {
  max-width: 100px; /* 根据需要调整图片大小 */
}
a {
  color: #2a60c9;
}
</style>