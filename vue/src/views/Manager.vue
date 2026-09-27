<template>
  <div class="manager-container">
    <div class="manager-header">
      <div class="manager-header-left clickable" @click="$router.push('/home')">
        <img src="@/assets/logo.svg" />
        <div class="title">管理システム</div>
      </div>

      <div class="manager-header-center">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/' }">ホーム</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="manager-header-right">
        <el-dropdown placement="bottom" class="profile-dropdown">
          <div class="avatar profile-trigger" tabindex="0">
            <img :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            <div class="profile-name">{{ user.name ||  '管理者' }}<i class="el-icon-arrow-down"></i></div>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="$router.push('/person')">個人情報</el-dropdown-item>
            <el-dropdown-item @click.native="logout">ログアウト</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <div class="manager-main">
      <div class="manager-main-left">
        <el-menu :default-openeds="['info', 'user', 'system']" router style="border: none" :default-active="$route.path">
          <el-menu-item index="/home">
            <i class="el-icon-s-home"></i>
            <span slot="title">ホーム</span>
          </el-menu-item>
          <el-submenu index="user" v-if="user.role == 'ADMIN'">
            <template slot="title">
              <i class="el-icon-s-custom"></i>
              <span>ユーザー管理</span>
            </template>
            <el-menu-item index="/admin"><i class="el-icon-user-solid"></i><span>管理者情報</span></el-menu-item>
            <el-menu-item index="/user"><i class="el-icon-user"></i><span>ユーザー情報</span></el-menu-item>
          </el-submenu>
          <el-submenu index="info" v-if="user.role == 'ADMIN'">
            <template slot="title">
              <i class="el-icon-s-data"></i>
              <span>情報管理</span>
            </template>
            <el-menu-item index="/type"><i class="el-icon-menu"></i><span>商品カテゴリ</span></el-menu-item>
            <el-menu-item index="/goods"><i class="el-icon-menu"></i><span>商品情報</span></el-menu-item>
            <el-menu-item index="/orders"><i class="el-icon-s-order"></i><span>注文情報</span></el-menu-item>
            <el-menu-item index="/notice"><i class="el-icon-notebook-2"></i><span>お知らせ</span></el-menu-item>
            <el-menu-item index="/carousel"><i class="el-icon-menu"></i><span>カルーセル画像</span></el-menu-item>
          </el-submenu>
          <el-submenu index="system">
            <template slot="title">
              <i class="el-icon-s-tools"></i>
              <span>システム管理</span>
            </template>
            <el-menu-item index="/person"><i class="el-icon-s-custom"></i><span slot="title">個人情報</span></el-menu-item>
          </el-submenu>
        </el-menu>
      </div>

      <div class="manager-main-right">
        <router-view @update:user="updateUser" />
      </div>
    </div>
  </div>
</template>
<script>

export default {
  name: 'HomeView',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
    }
  },
  mounted() {
    if (!this.user.id) {
      this.$router.push('/login')
    }
  },
  methods: {
    updateUser(user) {
      this.user = JSON.parse(JSON.stringify(user))
    },
    logout() {
      localStorage.removeItem('user')
      this.$router.push('/login')
    },
  }
}
</script>

<style>
@import "@/assets/css/manager.css";

</style>
