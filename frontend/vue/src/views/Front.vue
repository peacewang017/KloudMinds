<template>
  <div>

    <!--头部-->
    <div class="front-header">
      <div class="front-header-left">
        <img src="@/assets/imgs/logodi.png" alt="">
        <a href="/front/home">
          <div class="title" style="font-family: 'Futura PT', sans-serif;text-shadow: 2px 2px 4px #000000;font-size: 25px;">KloudMinds</div></a>
      </div>
      <div class="front-header-center">
        <div class="hcqFont hcqStyle2" style="color: #ebeddf; font-size: 26px; font-weight: 900; font-style: italic; padding-left: 52px;">
          "Smart, Efficient, Flexible Cloud Drive"
        </div>
      </div>
      <div class="front-header-right">
        <div v-if="!user.username">
          <el-button @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </div>
        <div v-else>
          <el-dropdown>
            <div class="front-header-dropdown">
              <img :src="user.avatar" alt="">
              <div style="margin-left: 10px; color: #fff">
                <span>{{ user.name }}</span><i class="el-icon-arrow-down" style="margin-left: 5px"></i>
              </div>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item>
                <div @click="$router.push('/front/person')">User Center</div>
              </el-dropdown-item>
              <el-dropdown-item>
                <div @click="logout">Logout</div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>
    <!--主体-->
    <div class="main-body">
      <router-view ref="child" @update:user="updateUser" />
    </div>
  </div>

</template>

<script>

export default {
  name: "FrontLayout",

  data () {
    return {
      top: '',
      notice: [],
      user: JSON.parse(localStorage.getItem("xm-user") || '{}'),
    }
  },

  mounted() {
    this.loadNotice()
  },
  methods: {
    loadNotice() {
      this.$request.get('/notice/selectAll').then(res => {
        this.notice = res.data
        let i = 0
        if (this.notice && this.notice.length) {
          this.top = this.notice[0].content
          setInterval(() => {
            this.top = this.notice[i].content
            i++
            if (i === this.notice.length) {
              i = 0
            }
          }, 2500)
        }
      })
    },
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('xm-user') || '{}')   // 重新获取下用户的最新信息
    },
    // 退出登录
    logout() {
      localStorage.removeItem("xm-user");
      this.$router.push("/login");
    },
  }

}
</script>
<style>
@import "@/assets/css/front.css";
</style>
<style scoped>

  .hcqFont {
    position: relative;
    letter-spacing: .07em;
    font-size: 3em;
    font-weight: normal;
    margin: 0 auto;
  }
  .hcqFont:before, .hcqFont:after {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
  }
  .hcqStyle2 {
    display: inline-block;
    font-weight: bold;
    color: #def;
    text-shadow: 0 0 1px currentColor, -1px -1px 1px #334775, 0 -1px 1px #334775, 1px -1px 1px #334775, 1px 0 1px #334775, 1px 1px 1px #334775, 0 1px 1px #334775, -1px 1px 1px #334775, -1px 0 1px #334775;
    -webkit-filter: url(#diff1);
    filter: url(#diff1);
  }
</style>