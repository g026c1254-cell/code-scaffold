<template>
  <div>
    <el-row :gutter="24">
      <el-col :span="24">
        <el-card style="margin: 10px 0">
          <div style="font-size: 17px;font-weight: 700">大家好，欢迎使用本系统！</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10">
      <el-col :span="12">
        <el-card class="box-card">
          <div class="clearfix" style="margin-bottom: 20px">
            <span style="font-size: 20px;font-weight: bold">公告信息</span>
          </div>
          <div class="text item">
            <el-collapse v-model="activeNames" @change="handleChange">
              <el-collapse-item :title="item.name" :name="index" v-for="(item,index) in notices" :key="index">
                <div>{{item.content}}</div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="box-card">
          <div ref="salesChart" style="width: 100%;height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from "echarts"

export default {
  name: "Home",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      activeNames: [0],
      notices: []
    }
  },
  created() {
    this.loadNotice()
  },
  mounted() {
    this.$request.get('/goods/echarts').then(res => {
      this.initPie(Array.isArray(res.data) ? res.data : [])
    }).catch(() => {
      this.initPie([])
    })
  },
  methods: {
    handleChange(val) {
      console.log(val);
    },
    loadNotice(){
      this.$request.get('/notice/selectAll').then(res => {
        this.notices = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.notices = []
      })
    },
    initPie(data){
      const chartDom = this.$refs.salesChart
      if (!chartDom) return
      if (this.chart) {
        this.chart.dispose()
      }
      this.chart = echarts.init(chartDom)
      this.chart.setOption({
        title: {
          text: '商品销量统计',
          subtext: '饼图',
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
        graphic: data.length ? [] : [{
          type: 'text',
          left: 'center',
          top: 'middle',
          style: {
            text: '暂无销量数据',
            fill: '#909399',
            fontSize: 16
          }
        }],
        series: [
          {
            type: 'pie',
            radius: '60%',
            data: data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      })
    }
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
    }
  }
}
</script>

<style scoped>
.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}


</style>