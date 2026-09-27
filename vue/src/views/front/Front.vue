<template>
  <div class="front-layout">
    <div class="header">
      <div class="front-header">
        <a href="/front/home" @click.prevent="$router.push('/front/home')">
          <div class="front-header-left">
            <img src="@/assets/logo.svg" alt="桑都安">
            <div class="brand-copy">
              <div class="title">桑都安 <span>- SOUTOYASU -</span></div>
              <div class="brand-subtitle">八王子学生リユース</div>
            </div>
          </div>
        </a>

        <div class="front-header-center">
          <div @click="goPage(item.path)" class="menu-item" v-for="item in menuList" :key="item.key" :class="{'menu-item-active' : item.path === $route.path }">{{ $t('nav.' + item.key) }}</div>
        </div>

        <form class="header-search" @submit.prevent="searchGoods">
          <el-input
              v-model="searchKeyword"
              class="header-search-input"
              clearable
              prefix-icon="el-icon-search"
              :placeholder="$t('common.searchProducts')">
          </el-input>
        </form>

        <div class="front-header-right">
          <div v-if="!user.username" class="front-header-right-button">
            <el-button type="primary" plain @click="$router.push('/login')">{{ $t('nav.login') }}</el-button>
            <el-button type="success" plain @click="$router.push('/register')">{{ $t('nav.register') }}</el-button>
          </div>
          <!-- 登录展示 -->
          <div v-else class="front-user-area">
            <el-dropdown class="profile-dropdown">
              <div class="front-header-dropdown profile-trigger" tabindex="0">
                <img :src="getAvatarUrl(user.avatar)" alt=""><i class="el-icon-arrow-down"></i>
              </div>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item>
                  <div style="color: #333">{{user.name}}</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div style="color: #333" @click="$router.push('/front/profile')">{{ $t('common.personalInfo') }}</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div @click="$router.push('/front/collect')">{{ $t('common.collection') }}</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div @click="$router.push('/front/orders')">{{ $t('common.orders') }}</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div @click="logout">{{ $t('nav.logout') }}</div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <div class="main-body">
      <router-view ref="child" @update:user="updateUser" />
    </div>

    <!-- 移动端专属底部导航栏 (Mobile Bottom TabBar) -->
    <nav class="mobile-bottom-bar">
      <div class="mobile-tab-item" :class="{ 'active': $route.path === '/front/home' }" @click="goPage('/front/home')">
        <i class="el-icon-s-home"></i>
        <span>{{ $t('nav.home') }}</span>
      </div>
      <div class="mobile-tab-item" :class="{ 'active': $route.path === '/front/goods' }" @click="goPage('/front/goods')">
        <i class="el-icon-s-goods"></i>
        <span>{{ $t('nav.goods') }}</span>
      </div>
      <div class="mobile-tab-item" :class="{ 'active': $route.path === '/front/orders' }" @click="handleProtectedNav('/front/orders')">
        <i class="el-icon-s-order"></i>
        <span>{{ $t('common.orders') }}</span>
      </div>
      <div class="mobile-tab-item" :class="{ 'active': $route.path === '/front/person' || $route.path === '/front/profile' }" @click="handleProtectedNav('/front/person')">
        <i class="el-icon-user-solid"></i>
        <span>{{ $t('nav.person') }}</span>
      </div>
    </nav>

    <Footer />
  </div>
</template>

<script>

import Footer from "@/conponents/Footer.vue";
import { createPixelAvatar, createPixelAvatarId } from '@/utils/pixelAvatar'

export default {
  name: "FrontLayout",
  components: {Footer},
  data () {
    return {
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      searchKeyword: '',
      menuList: [
        {key: "home", path: '/front/home'},
        {key: "goods", path: '/front/goods'},
        {key: "person", path: '/front/person'},
      ],
    }
  },
  created() {
    if (this.user.username && !this.user.avatar) {
      this.user.avatar = createPixelAvatarId(this.user.id)
      localStorage.setItem('user', JSON.stringify(this.user))
      this.$request.put('/user/avatar', { avatar: this.user.avatar }).catch(() => {})
    }
    if(typeof this.user.username === 'undefined'){
      this.menuList = this.menuList.filter(item => item.key === "home");
    }
  },
  methods: {
    getAvatarUrl(avatar) {
      if (!avatar) return require('@/assets/empty.svg')
      if (String(avatar).indexOf('pixel:') === 0) {
        return createPixelAvatar(String(avatar).slice(6))
      }
      return avatar
    },
    goPage(path) {
      if (this.$route.path !== path) {
        this.$router.push(path)
      }
    },
    handleProtectedNav(path) {
      if (!this.user.username) {
        this.$confirm('この機能を利用するにはログインが必要です。ログインページに移動しますか？', 'ログイン案内', {
          confirmButtonText: 'ログイン',
          cancelButtonText: 'キャンセル',
          type: 'info'
        }).then(() => {
          this.$router.push('/login')
        }).catch(() => {})
        return
      }
      this.goPage(path)
    },
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('user') || '{}')   // 重新获取下用户的最新信息
    },
    logout() {
      localStorage.removeItem("user");
      this.$router.push('/front/home')
    },
    searchGoods() {
      const keyword = this.searchKeyword.trim()
      this.$router.push({
        path: '/front/goods',
        query: keyword ? { name: keyword } : {}
      })
    }
  },
}
</script>

<style scoped>
@import "@/assets/css/front.css";

</style>
