import Vue from 'vue'
import VueRouter from 'vue-router'

// 解决重复点击路由报错问题
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    redirect: '/home',
    children: [
      { path: '403', name: 'Auth', meta: { name: 'アクセス権限なし' }, component: () => import('../views/manager/Auth.vue') },
      { path: 'home', name: 'ManagerHome', meta: { name: 'システムホーム' }, component: () => import('../views/manager/Home.vue') },
      { path: 'admin', name: 'Admin', meta: { name: '管理者情報', requireAdmin: true }, component: () => import('../views/manager/Admin.vue') },
      { path: 'user', name: 'User', meta: { name: 'ユーザー情報', requireAdmin: true }, component: () => import('../views/manager/User.vue') },
      { path: 'person', name: 'ManagerPerson', meta: { name: '個人情報' }, component: () => import('../views/manager/Person.vue') },
      { path: 'type', name: 'Type', meta: { name: '商品カテゴリ' }, component: () => import('../views/manager/Type.vue') },
      { path: 'goods', name: 'Goods', meta: { name: '商品情報' }, component: () => import('../views/manager/Goods.vue') },
      { path: 'orders', name: 'ManagerOrders', meta: { name: '注文情報' }, component: () => import('../views/manager/Orders.vue') },
      { path: 'notice', name: 'Notice', meta: { name: 'お知らせ' }, component: () => import('../views/manager/Notice.vue') },
      { path: 'Carousel', name: 'Carousel', meta: { name: 'カルーセル画像' }, component: () => import('../views/manager/Carousel.vue') },
    ]
  },
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/front/Front.vue'),
    redirect: '/front/home',
    children: [
      { path: 'home', name: 'FrontHome', meta: { name: 'ホーム' }, component: () => import('../views/front/Home.vue') },
      { path: 'person', name: 'FrontPerson', meta: { name: 'マイページ', requiresAuth: true }, component: () => import('../views/front/Person.vue') },
      { path: 'profile', name: 'FrontProfile', meta: { name: '個人情報', requiresAuth: true }, component: () => import('../views/front/Profile.vue') },
      { path: 'password', name: 'Password', meta: { name: 'パスワード変更', requiresAuth: true }, component: () => import('../views/front/Password.vue') },
      {path: 'goods', name: 'FrontGoods', meta: { name: '商品一覧' }, component: () => import('../views/front/Goods.vue')},
      {path: 'goodsDetail', name: 'GoodsDetail', meta: { name: '商品詳細' }, component: () => import('../views/front/GoodsDetail.vue')},
      {path: 'collect', name: 'Collect', meta: { name: 'お気に入り', requiresAuth: true }, component: () => import('../views/front/Collect.vue')},
      {path: 'orders', name: 'FrontOrders', meta: { name: '注文履歴', requiresAuth: true }, component: () => import('../views/front/Orders.vue')},
    ]
  },
  { path: '/login', name: 'Login', meta: { name: 'ログイン' }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: '新規登録' }, component: () => import('../views/Register.vue') },
  { path: '*', name: 'page-404', meta: { name: 'ページが見つかりません' }, component: () => import('../views/404.vue')},
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  let user = {}
  try {
    user = JSON.parse(localStorage.getItem('user') || '{}')
  } catch (error) {
    localStorage.removeItem('user')
  }
  if (to.path === '/'){
    if (user.role){
      if (user.role === 'ADMIN'){
        next('/home')
      } else {
        next('/front/home')
      }
    } else {
      next('/login')
    }
  } else if (to.matched.length === 0) {
    next('/404')
  } else if (to.matched.some(record => record.meta.requiresAuth) && (!user.id || !user.token)) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
