<template>
  <div class="container">
    <div style="height: 60px; position: fixed; top: 0; display: flex; align-items: center; padding-left: 20px">
      <img src="@/assets/imgs/logo.png" alt="" style="width: 50px;transform: scale(1.5);margin-top: 15px">
      <span style="font-family: 'Futura PT', sans-serif;color: #feac2c; font-size: 24px; font-weight: bold; margin-left: 65px;margin-top: 15px;transform: scale(1.5); text-shadow: 1px 2px 5px black;">KloudMinds</span>
    </div>

    <div style="flex: 1; display: flex; justify-content: center">
      <img src="@/assets/imgs/log.svg" alt="" style="width: 80%">
    </div>

    <div style="flex: 1; display: flex; justify-content: center">
      <div style="width: 300px; position: relative; padding: 50px 30px; box-shadow: 0 0 10px #667db3; background-color: #feefc7;margin-right: 155px; border-radius: 35px;">
        <div style="position: absolute; background-color: white; top: 100px; z-index: 999" v-if="slideVerifyShow">
          <slide-verify :l="42"
                        :r="10"
                        :w="300"
                        :h="155"
                        :accuracy="5"
                        :imgs="imgs"
                        slider-text="Swipe right"
                        @success="onSuccess"
                        ref="slideVerifyRef"
          ></slide-verify>
        </div>
        <div class="futura-bold" style="text-align: center; font-size: 24px; margin-bottom: 30px; color: #333;">
          WELCOME BACK
        </div>
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="username">
            <el-input size="medium" prefix-icon="el-icon-user" placeholder="Please enter your account" v-model="form.username" class="biao"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input size="medium" prefix-icon="el-icon-lock" placeholder="Please enter your password" show-password  v-model="form.password" class="biao"></el-input>
          </el-form-item>
          <el-form-item prop="role">
            <el-select style="width: 100%" size="medium" v-model="form.role" class="biao">
              <el-option value="ADMIN" label="Manager"></el-option>
              <el-option value="USER" label="User"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button size="medium" style="width: 100%; background-color: #b0c8fe; border-color: #6184d5; color: #3e64be" @click="login">Login</el-button>
          </el-form-item>

          <div style="display: flex; align-items: center">
            <div style="flex: 1"></div>
            <div style="display: flex; flex-direction: column; align-items: center; text-align: center; flex-wrap: wrap;margin-right: 28px">
              Don't have an account yet?
              <div style="display: inline-flex; align-items: center;">
                Please&nbsp<a href="/register"> register</a>
              </div>
            </div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      dialogVisible: true,
      form: { role: 'ADMIN' },
      rules: {
        username: [
          { required: true, message: 'Account cannot be empty', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'Password cannot be empty', trigger: 'blur' },
        ]
      },
      imgs: [
        require('@/assets/imgs/1.jpg'),
        require('@/assets/imgs/2.jpg'),
        require('@/assets/imgs/3.jpg'),
      ],
      slideVerifyShow: false
    }
  },
  created() {

  },
  methods: {
    onSuccess() {
      this.$request.post('/login', this.form).then(res => {
        if (res.code === '200') {
          localStorage.setItem("xm-user", JSON.stringify(res.data))  // 存储用户数据
          this.$message.success('Login succeeded')
          setTimeout(() => {
            if (res.data.role === 'ADMIN') {
              location.href = '/home'
            } else {
              location.href = '/front/home?category=all'
            }
          }, 500)
        } else {
          this.$message.error(res.msg)
          this.slideVerifyShow = false
        }
      })
    },
    login() {
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          this.slideVerifyShow = true
          // 验证通过

        }
      })
    }
  }
}
</script>

<style scoped>
/deep/ .biao{
  background-color: #cccccc;
}
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
