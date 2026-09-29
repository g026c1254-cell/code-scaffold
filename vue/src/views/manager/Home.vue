<template>
  <div class="manager-dashboard">
    <!-- 顶部欢迎卡片 -->
    <el-card class="welcome-card" :body-style="{ padding: '20px 24px' }">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2 class="welcome-title">ようこそ、{{ user.name || user.username || '管理者' }} 様</h2>
          <p class="welcome-subtitle">八王子・桑都安（SOUTOYASU）管理システムへようこそ。リアルタイムな運営状況をご確認いただけます。</p>
        </div>
        <div class="welcome-badge">
          <span class="badge-dot"></span>
          <span>システム正常稼働中</span>
        </div>
      </div>
    </el-card>

    <!-- 统计指标网格（新增：総売上金額、本日売上金額） -->
    <el-row :gutter="14" class="stat-row">
      <!-- 1. 総売上金額 (Total Sales) -->
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card stat-card-sales" @click="$router.push('/orders')">
          <div class="stat-icon-wrap icon-totalsales">
            <i class="el-icon-wallet"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">総売上金額</div>
            <div class="stat-value text-sales">¥{{ formatNumber(stat.totalSales) }}<span class="stat-unit">円</span></div>
          </div>
        </div>
      </el-col>

      <!-- 2. 本日売上金額 (Today Sales) -->
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card stat-card-today" @click="$router.push('/orders')">
          <div class="stat-icon-wrap icon-todaysales">
            <i class="el-icon-coin"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">本日売上金額</div>
            <div class="stat-value text-today">¥{{ formatNumber(stat.todaySales) }}<span class="stat-unit">円</span></div>
          </div>
        </div>
      </el-col>

      <!-- 3. 注文総数 -->
      <el-col :xs="12" :sm="8" :md="4">
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

      <!-- 4. 商品総数 -->
      <el-col :xs="12" :sm="8" :md="4">
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

      <!-- 5. 登録ユーザー -->
      <el-col :xs="12" :sm="8" :md="4">
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

      <!-- 6. カテゴリ数 -->
      <el-col :xs="12" :sm="8" :md="4">
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

    <!-- 中部区域：销量饼状图 + 快捷菜单 -->
    <el-row :gutter="16" class="middle-row">
      <!-- 销量饼状图 (Sales Volume Pie Chart) -->
      <el-col :xs="24" :lg="15">
        <el-card class="dashboard-panel chart-panel" :body-style="{ padding: '16px 20px' }">
          <div slot="header" class="panel-header">
            <div class="panel-title">
              <span class="title-accent"></span>
              <span>商品別販売数量シェア（売上比率）</span>
            </div>
            <div class="panel-extra">
              <el-tag size="small" type="success" effect="plain" v-if="hasSalesData">
                総販売点数: {{ totalUnitsSold }} 点
              </el-tag>
              <el-tag size="small" type="info" effect="plain" v-else>
                販売集計中
              </el-tag>
            </div>
          </div>
          <div class="chart-wrapper">
            <div ref="pieChartRef" class="echarts-box"></div>
            <div v-if="!hasSalesData" class="chart-empty-placeholder">
              <el-empty description="販売データがまだありません" :image-size="90"></el-empty>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 快捷菜单 (Quick Links) -->
      <el-col :xs="24" :lg="9">
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

    <!-- 下方内容区域：最新公告（通知内容以正常富文本格式渲染） -->
    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :md="24">
        <el-card class="dashboard-panel" :body-style="{ padding: '16px 20px' }">
          <div slot="header" class="panel-header">
            <div class="panel-title">
              <span class="title-accent"></span>
              <span>{{ $t('common.notice') }}</span>
            </div>
            <el-button type="text" size="small" @click="$router.push('/notice')">
              すべて見る<i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
          <div class="notice-list-wrap">
            <el-collapse v-if="notices.length > 0" v-model="activeNames">
              <el-collapse-item :name="index" v-for="(item, index) in notices" :key="index">
                <template slot="title">
                  <span class="collapse-title-text">{{ item.name }}</span>
                </template>
                <!-- 通知内容以正常格式（HTML富文本解析渲染）显示 -->
                <div class="notice-body-text" v-html="formatNoticeContent(item.content)"></div>
                <div class="notice-meta-bar">
                  <span class="notice-meta-item"><i class="el-icon-user"></i> {{ item.userName || 'システム' }}</span>
                  <span class="notice-meta-item" v-if="item.time"><i class="el-icon-time"></i> {{ item.time }}</span>
                </div>
              </el-collapse-item>
            </el-collapse>
            <el-empty v-else description="お知らせはありません" :image-size="100"></el-empty>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: "ManagerHome",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      activeNames: [0],
      notices: [],
      stat: {
        totalSales: 0,
        todaySales: 0,
        goodsCount: 0,
        ordersCount: 0,
        userCount: 0,
        typeCount: 0
      },
      salesChartData: [],
      totalUnitsSold: 0,
      hasSalesData: false,
      pieChart: null
    }
  },
  created() {
    this.loadNotice()
    this.loadStats()
  },
  mounted() {
    this.handleResize = () => {
      if (this.pieChart) {
        this.pieChart.resize()
      }
    }
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    if (this.handleResize) {
      window.removeEventListener('resize', this.handleResize)
    }
    if (this.pieChart) {
      this.pieChart.dispose()
      this.pieChart = null
    }
  },
  methods: {
    formatNumber(num) {
      if (num === null || num === undefined || isNaN(num)) return '0'
      return Number(num).toLocaleString()
    },
    formatNoticeContent(content) {
      if (!content) return ''
      // 如果已包含HTML富文本标签，直接渲染
      if (/<[a-z][\s\S]*>/i.test(content)) {
        return content
      }
      // 如果是纯文本，将换行符替换为<br>以正常换行排版
      return content.replace(/\r?\n/g, '<br>')
    },
    loadNotice() {
      this.$request.get('/notice/selectAll').then(res => {
        this.notices = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.notices = []
      })
    },
    loadStats() {
      // 1. 商品总数
      this.$request.get('/goods/selectAll').then(res => {
        this.stat.goodsCount = Array.isArray(res.data) ? res.data.length : 0
      }).catch(() => {})

      // 2. 注册用户数
      this.$request.get('/user/selectAll').then(res => {
        this.stat.userCount = Array.isArray(res.data) ? res.data.length : 0
      }).catch(() => {})

      // 3. 分类数
      this.$request.get('/type/selectAll').then(res => {
        this.stat.typeCount = Array.isArray(res.data) ? res.data.length : 0
      }).catch(() => {})

      // 4. 订单数据、总售出金额、今日售出金额及销量饼状图统计
      this.$request.get('/orders/selectAll').then(res => {
        const orders = Array.isArray(res.data) ? res.data : []
        this.stat.ordersCount = orders.length

        // 筛选已付款订单进行售出金额与销量统计（若历史数据全未设置已支付状态，则以有效订单计算）
        const paidOrders = orders.filter(o => o.state === '已支付' || o.state === '支払い済み')
        const validOrders = paidOrders.length > 0 ? paidOrders : orders

        // 计算总售出金额 (Total Sales Amount)
        const total = validOrders.reduce((sum, o) => sum + (Number(o.price) || 0), 0)
        this.stat.totalSales = Math.round(total)

        // 计算今日售出金额 (Today Sales Amount)
        const now = new Date()
        const yyyy = now.getFullYear()
        const mm = String(now.getMonth() + 1).padStart(2, '0')
        const dd = String(now.getDate()).padStart(2, '0')
        const todayStr = `${yyyy}-${mm}-${dd}`

        const todayOrders = validOrders.filter(o => o.time && o.time.startsWith(todayStr))
        const todayTotal = todayOrders.reduce((sum, o) => sum + (Number(o.price) || 0), 0)
        this.stat.todaySales = Math.round(todayTotal)

        // 销量统计（按商品名称归类累加销售件数 nums）
        const volumeMap = {}
        validOrders.forEach(o => {
          const productName = (o.name || 'その他商品').trim()
          const qty = Number(o.nums) || 1
          volumeMap[productName] = (volumeMap[productName] || 0) + qty
        })

        let list = Object.keys(volumeMap).map(key => ({
          name: key,
          value: volumeMap[key]
        })).sort((a, b) => b.value - a.value)

        // 若商品超过 7 种，将第 7 种之后归入「その他」
        if (list.length > 7) {
          const topList = list.slice(0, 6)
          const othersCount = list.slice(6).reduce((acc, curr) => acc + curr.value, 0)
          topList.push({ name: 'その他', value: othersCount })
          list = topList
        }

        this.salesChartData = list
        this.totalUnitsSold = list.reduce((sum, item) => sum + item.value, 0)
        this.hasSalesData = list.length > 0

        this.$nextTick(() => {
          this.initSalesPieChart()
        })
      }).catch(err => {
        console.error(err)
      })
    },
    initSalesPieChart() {
      const container = this.$refs.pieChartRef
      if (!container) return

      if (!this.pieChart) {
        this.pieChart = echarts.init(container)
      }

      if (!this.hasSalesData) {
        this.pieChart.clear()
        return
      }

      const option = {
        title: {
          text: '総販売点数',
          subtext: this.totalUnitsSold + ' 点',
          left: 'center',
          top: '36%',
          textStyle: {
            fontSize: 13,
            color: '#64748b',
            fontWeight: 'normal'
          },
          subtextStyle: {
            fontSize: 22,
            color: '#1e293b',
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}<br/><b>{c} 点</b> ({d}%)',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e2e8f0',
          borderWidth: 1,
          padding: [8, 12],
          textStyle: { color: '#334155', fontSize: 13 },
          extraCssText: 'box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08); border-radius: 8px;'
        },
        legend: {
          bottom: 4,
          left: 'center',
          itemWidth: 10,
          itemHeight: 10,
          icon: 'circle',
          textStyle: {
            color: '#64748b',
            fontSize: 12
          }
        },
        color: [
          '#ff7e29',
          '#3b82f6',
          '#10b981',
          '#8b5cf6',
          '#f59e0b',
          '#06b6d4',
          '#ec4899',
          '#64748b'
        ],
        series: [
          {
            name: '販売数量',
            type: 'pie',
            radius: ['42%', '68%'],
            center: ['50%', '42%'],
            avoidLabelOverlap: true,
            itemStyle: {
              borderRadius: 6,
              borderColor: '#ffffff',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b}: {c}点\n({d}%)',
              fontSize: 12,
              color: '#475569'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 13,
                fontWeight: 'bold',
                color: '#0f172a'
              },
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.12)'
              }
            },
            data: this.salesChartData
          }
        ]
      }

      this.pieChart.setOption(option, true)
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
  padding: 14px 12px;
  display: flex;
  align-items: center;
  gap: 12px;
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

.stat-card-sales {
  border-color: #bbf7d0;
  background: linear-gradient(180deg, #f0fdf4 0%, #ffffff 100%);
}

.stat-card-today {
  border-color: #fed7aa;
  background: linear-gradient(180deg, #fff7ed 0%, #ffffff 100%);
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.icon-totalsales {
  background: #dcfce7;
  color: #15803d;
}

.icon-todaysales {
  background: #ffedd5;
  color: #ea580c;
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
  min-width: 0;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.text-sales {
  color: #15803d;
}

.text-today {
  color: #ea580c;
}

.stat-unit {
  font-size: 12px;
  font-weight: 400;
  color: #94a3b8;
  margin-left: 3px;
}

/* 中部区域与图表 */
.middle-row {
  margin-top: 4px;
}

.dashboard-panel {
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 4px 16px rgba(15, 23, 42, .03);
  margin-bottom: 12px;
}

.chart-panel {
  min-height: 380px;
}

.chart-wrapper {
  position: relative;
  width: 100%;
  height: 320px;
}

.echarts-box {
  width: 100%;
  height: 100%;
}

.chart-empty-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
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
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.title-accent {
  width: 4px;
  height: 16px;
  background: #ff8a3d;
  border-radius: 2px;
}

/* 通知列表正常格式展示 */
.notice-list-wrap {
  min-height: 120px;
}

.collapse-title-text {
  font-weight: 600;
  color: #334155;
  font-size: 14px;
}

.notice-body-text {
  color: #475569;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
  padding: 8px 4px;
}

.notice-body-text >>> img {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 6px 0;
}

.notice-body-text >>> p {
  margin: 4px 0;
}

.notice-meta-bar {
  margin-top: 10px;
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.notice-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  background: #f1f5f9;
  border-radius: 10px;
  font-size: 11px;
  color: #64748b;
}

.notice-meta-item i {
  color: #ff8a3d;
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
  padding: 20px 8px;
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
  font-size: 26px;
  color: #ea6b1f;
}

.quick-link-item span {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
}

@media (max-width: 768px) {
  .quick-links-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .stat-value {
    font-size: 16px;
  }
  .chart-wrapper {
    height: 280px;
  }
}
</style>
