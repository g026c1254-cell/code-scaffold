<template>
  <div class="manager-dashboard">
    <!-- 顶部欢迎卡片 -->
    <el-card class="welcome-card" :body-style="{ padding: '20px 24px' }">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2 class="welcome-title">ようこそ、{{ user.name || '管理者' }} 様</h2>
          <p class="welcome-subtitle">桑都安（SOUTOYASU）管理システムへようこそ。リアルタイムな運営状況をご確認いただけます。</p>
        </div>
        <div class="welcome-badge">
          <span class="badge-dot"></span>
          <span>システム正常稼働中</span>
        </div>
      </div>
    </el-card>

    <!-- 统计指标网格 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card" @click="$router.push('/goods')">
          <div class="stat-icon-wrap icon-goods">
            <i class="el-icon-goods"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">商品総数</div>
            <div class="stat-value">{{ stat.goodsCount }}<span class="stat-unit">件</span></div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card" @click="$router.push('/orders')">
          <div class="stat-icon-wrap icon-orders">
            <i class="el-icon-s-order"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">注文総数</div>
            <div class="stat-value">{{ stat.ordersCount }}<span class="stat-unit">件</span></div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card" @click="$router.push('/user')">
          <div class="stat-icon-wrap icon-user">
            <i class="el-icon-user-solid"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">登録ユーザー</div>
            <div class="stat-value">{{ stat.userCount }}<span class="stat-unit">名</span></div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card" @click="$router.push('/type')">
          <div class="stat-icon-wrap icon-type">
            <i class="el-icon-menu"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">カテゴリ数</div>
            <div class="stat-value">{{ stat.typeCount }}<span class="stat-unit">種</span></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 下方内容区域：最新公告与快捷入口 -->
    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :md="15">
        <el-card class="dashboard-panel" :body-style="{ padding: '16px 20px' }">
          <div slot="header" class="panel-header">
            <div class="panel-title">
              <span class="title-accent"></span>
              <span>{{ $t('common.notice') }}</span>
            </div>
            <el-button type="text" size="small" @click="$router.push('/notice')">すべて見る<i class="el-icon-arrow-right"></i></el-button>
          </div>
          <div class="notice-list-wrap">
            <el-collapse v-if="notices.length > 0" v-model="activeNames">
              <el-collapse-item :name="index" v-for="(item, index) in notices" :key="index">
                <template slot="title">
                  <span class="collapse-title-text">{{ item.name }}</span>
                </template>
                <div class="notice-body-text">{{ item.content }}</div>
                <div class="notice-meta-tag">投稿者：{{ item.userName || 'システム' }}</div>
              </el-collapse-item>
            </el-collapse>
            <el-empty v-else description="お知らせはありません" :image-size="100"></el-empty>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="9">
        <el-card class="dashboard-panel" :body-style="{ padding: '16px 20px' }">
          <div slot="header" class="panel-header">
            <div class="panel-title">
              <span class="title-accent"></span>
              <span>クイックメニュー</span>
            </div>
          </div>
          <div class="quick-links-grid">
            <div class="quick-link-item" @click="$router.push('/goods')">
              <i class="el-icon-s-goods"></i>
              <span>商品管理</span>
            </div>
            <div class="quick-link-item" @click="$router.push('/orders')">
              <i class="el-icon-s-order"></i>
              <span>注文一覧</span>
            </div>
            <div class="quick-link-item" @click="$router.push('/user')">
              <i class="el-icon-user"></i>
              <span>ユーザー管理</span>
            </div>
            <div class="quick-link-item" @click="$router.push('/type')">
              <i class="el-icon-folder"></i>
              <span>カテゴリ設定</span>
            </div>
            <div class="quick-link-item" @click="$router.push('/notice')">
              <i class="el-icon-bell"></i>
              <span>お知らせ投稿</span>
            </div>
            <div class="quick-link-item" @click="$router.push('/person')">
              <i class="el-icon-setting"></i>
              <span>個人設定</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "ManagerHome",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      activeNames: [0],
      notices: [],
      stat: {
        goodsCount: 0,
        ordersCount: 0,
        userCount: 0,
        typeCount: 0
      }
    }
  },
  created() {
    this.loadNotice()
    this.loadStats()
  },
  methods: {
    loadNotice() {
      this.$request.get('/notice/selectAll').then(res => {
        this.notices = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.notices = []
      })
    },
    loadStats() {
      // 聚合数据统计
      this.$request.get('/goods/selectAll').then(res => {
        this.stat.goodsCount = Array.isArray(res.data) ? res.data.length : 0
      }).catch(() => {})

      this.$request.get('/orders/selectAll').then(res => {
        this.stat.ordersCount = Array.isArray(res.data) ? res.data.length : 0
      }).catch(() => {})

      this.$request.get('/user/selectAll').then(res => {
        this.stat.userCount = Array.isArray(res.data) ? res.data.length : 0
      }).catch(() => {})

      this.$request.get('/type/selectAll').then(res => {
        this.stat.typeCount = Array.isArray(res.data) ? res.data.length : 0
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.manager-dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 顶部欢迎横幅 */
.welcome-card {
  border-radius: 12px;
  background: linear-gradient(135deg, #fff7ed 0%, #ffffff 100%);
  border: 1px solid #fed7aa;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.welcome-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.welcome-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #64748b;
}

.welcome-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 20px;
  font-size: 12px;
  color: #166534;
  font-weight: 600;
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.95); opacity: 0.8; }
  50% { transform: scale(1.15); opacity: 1; }
  100% { transform: scale(0.95); opacity: 0.8; }
}

/* 指标卡片 */
.stat-row {
  margin-top: 4px;
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, .8);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(15, 23, 42, .03);
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;
  margin-bottom: 12px;
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: #ff8a3d;
  box-shadow: 0 8px 20px rgba(255, 126, 41, .12);
}

.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.icon-goods {
  background: #fff7ed;
  color: #ea6b1f;
}

.icon-orders {
  background: #eff6ff;
  color: #2563eb;
}

.icon-user {
  background: #f0fdf4;
  color: #16a34a;
}

.icon-type {
  background: #faf5ff;
  color: #9333ea;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-top: 2px;
}

.stat-unit {
  font-size: 13px;
  font-weight: 400;
  color: #94a3b8;
  margin-left: 4px;
}

/* 内容板块 */
.dashboard-panel {
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 4px 16px rgba(15, 23, 42, .03);
  min-height: 320px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.title-accent {
  width: 4px;
  height: 16px;
  background: #ff8a3d;
  border-radius: 2px;
}

.collapse-title-text {
  font-weight: 600;
  color: #334155;
}

.notice-body-text {
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

.notice-meta-tag {
  margin-top: 8px;
  display: inline-block;
  padding: 2px 8px;
  background: #f1f5f9;
  border-radius: 10px;
  font-size: 11px;
  color: #64748b;
}

/* 快捷菜单 */
.quick-links-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.quick-link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 10px;
  background: #faf8f5;
  border: 1px solid #fed7aa;
  cursor: pointer;
  transition: all .2s ease;
  user-select: none;
}

.quick-link-item:hover {
  background: #fff7ed;
  border-color: #ff8a3d;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 126, 41, .15);
}

.quick-link-item i {
  font-size: 24px;
  color: #ea6b1f;
}

.quick-link-item span {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
}
</style>
