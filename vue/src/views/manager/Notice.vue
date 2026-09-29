<template>
  <div>
    <!-- 表格内容 -->
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px; margin: 0 5px" placeholder="お知らせのタイトルを検索" v-model="name"></el-input>
        <el-button type="success" plain @click="load(1)">検索</el-button>
        <el-button type="info" plain @click="reset">リセット</el-button>
        <el-button type="primary" plain @click="handleAdd">追加</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="番号" width="70" align="center">
          <template slot-scope='scope'>
            <span>{{ (pageNum - 1) * pageSize + (scope.$index + 1) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="お知らせタイトル" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column label="お知らせ内容" min-width="260" :show-overflow-tooltip="true">
          <template v-slot="scope">
            <span>{{ stripHtml(scope.row.content) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="追加日時" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="userId" label="追加者ID"></el-table-column>
        <el-table-column prop="userName" label="投稿者" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="views" label="閲覧数" width="85" align="center">
          <template v-slot="scope">{{ scope.row.views || 0 }}</template>
        </el-table-column>
        <el-table-column prop="likes" label="いいね" width="85" align="center">
          <template v-slot="scope">{{ scope.row.likes || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="240">
          <template v-slot="scope">
            <el-button size="mini" type="success" plain @click="detail(scope.row)">詳細</el-button>
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">編集</el-button>
            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">削除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin: 10px 0">
        <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[2, 5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 新增 | 编辑弹框 -->
    <el-dialog title="情報" :visible.sync="fromVisible" width="40%">
      <el-form :model="form" label-width="100px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="タイトル" prop="name">
          <el-input v-model="form.name" placeholder="お知らせタイトル"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" :rows="6" v-model="form.content" placeholder="お知らせ内容を入力してください（HTML対応）"></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">キャンセル</el-button>
        <el-button type="primary" @click="save">確定</el-button>
      </div>
    </el-dialog>

    <!-- 详情内容（富文本正常格式呈现） -->
    <el-drawer :visible.sync="formDetailVisible" title="詳細" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">お知らせ詳細</span>
        <div class="drawer-actions">
          <el-tooltip placement="top" :content="isFullscreen ? '全画面を終了' : '全画面'">
            <el-button icon="el-icon-full-screen" size="mini" circle @click="toggleFullscreen"/>
          </el-tooltip>
          <el-button icon="el-icon-close" size="mini" circle @click="formDetailVisible = false"/>
        </div>
      </div>

      <!-- 抽屉内容 -->
      <div class="drawer-content" ref="drawerContent">
        <el-form label-width="110px" style="padding-right: 20px" :model="form">
          <el-form-item label="お知らせタイトル" prop="name">
            <div style="font-weight: 600; font-size: 15px; color: #1e293b;">{{form.name}}</div>
          </el-form-item>
          <el-form-item label="お知らせ内容" prop="content">
            <!-- 正常格式解析并呈现 -->
            <div class="notice-detail-content" v-html="formatNoticeContent(form.content)"></div>
          </el-form-item>
          <el-form-item label="追加日時" prop="time">
            <div>{{form.time}}</div>
          </el-form-item>
          <el-form-item label="追加者ID" prop="userId">
            <div>{{form.userId}}</div>
          </el-form-item>
          <el-form-item label="投稿者" prop="userName">
            <div>{{form.userName || '匿名ユーザー'}}</div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 抽屉底部 -->
      <div class="drawer-footer">
        <el-button @click="formDetailVisible = false">閉じる</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: "Notice",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      name: '',
      total: 0,
      fromVisible: false,
      formDetailVisible: false,
      form: {},
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      rules: {
        name: [
          {required: true, message: 'お知らせタイトルを入力してください', trigger: 'blur'},
        ],
        content: [
          {required: true, message: 'お知らせ内容を入力してください', trigger: 'blur'},
        ]
      },
      isFullscreen: false,
      drawerWidth: '50%',
      drawerPosition: 'right'
    }
  },
  created() {
    this.load()
  },
  methods: {
    stripHtml(str) {
      if (!str) return ''
      return str.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').trim()
    },
    formatNoticeContent(content) {
      if (!content) return ''
      if (/<[a-z][\s\S]*>/i.test(content)) {
        return content
      }
      return content.replace(/\r?\n/g, '<br>')
    },
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/notice/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.tableData = res.data && Array.isArray(res.data.records) ? res.data.records : []
        this.total = res.data && res.data.total ? res.data.total : 0
      })
    },
    save() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.id ? '/notice/update' : '/notice/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '完了', message: '保存しました', showClose: false, duration: 2000});
              this.load(1)
              this.fromVisible = false
            } else {
              this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
    detail(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.formDetailVisible = true
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.fromVisible = true
    },
    handleAdd() {
      this.form = {}
      this.fromVisible = true
    },
    del(id) {
      this.$confirm('削除してもよろしいですか？', '削除確認', {type: "warning"}).then(response => {
        this.$request.delete('/notice/delete?id=' + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({title: '完了', message: '操作が完了しました', showClose: false, duration: 2000});
            this.load(1)
          } else {
            this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
          }
        })
      }).catch(() => {
      })
    },
    reset() {
      this.name = ''
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleFullscreenChange() {
      if (!document.fullscreenElement && this.isFullscreen) {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
      }
    },
    toggleFullscreen() {
      if (!this.isFullscreen) {
        this.originalWidth = this.drawerWidth;
        this.originalPosition = this.drawerPosition;
        this.isFullscreen = true;
        this.drawerWidth = '100%';
        this.drawerPosition = 'bottom';
        this.$nextTick(() => {
          this.requestFullscreen(this.$refs.drawerContent);
        });
      } else {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
        this.exitFullscreen();
      }
    },
    requestFullscreen(element) {
      if (element.requestFullscreen) {
        element.requestFullscreen();
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen();
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      }
    },
    exitFullscreen() {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
    }
  },
  mounted() {
    document.addEventListener('fullscreenchange', this.handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
  beforeDestroy() {
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
}
</script>

<style scoped>
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  background: #faf8f5;
}

.drawer-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.drawer-actions {
  display: flex;
  gap: 8px;
}

.drawer-content {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 130px);
}

.drawer-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 20px;
  border-top: 1px solid #f1f5f9;
  background: #faf8f5;
  display: flex;
  justify-content: flex-end;
}

/* 通知内容富文本正常格式化展示 */
.notice-detail-content {
  line-height: 1.7;
  color: #334155;
  font-size: 14px;
  word-break: break-word;
  background: #f8fafc;
  padding: 14px 18px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.notice-detail-content >>> img {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 6px 0;
}

.notice-detail-content >>> p {
  margin: 6px 0;
}

.notice-detail-content >>> table {
  width: 100%;
  border-collapse: collapse;
}

.notice-detail-content >>> th,
.notice-detail-content >>> td {
  border: 1px solid #cbd5e1;
  padding: 6px 10px;
}
</style>
