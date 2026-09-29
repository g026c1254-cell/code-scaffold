<template>
  <div class="manager-goods-page">
    <!-- 顶部+搜索框 -->
    <div class="goods-header-bar">
      <div>
        <h1 class="page-title">商品情報</h1>
      </div>
      <div class="search-box-wrap">
        <input v-model='keyboard' type="text" placeholder="商品名を検索" class="search-input" @keyup.enter="loadGoods"/>
        <el-button class="search-button" @click="loadGoods">
          <i class="el-icon-search"></i>
        </el-button>
      </div>
    </div>

    <!-- 分类按钮：完全透明无背景色风格 -->
    <div class="category-filter-area">
      <div class="type-group">
        <button
          type="button"
          class="type-chip-btn"
          :class="{ 'type-selected': selectedCategoryId === 0 }"          @click="handleAllClick">
          すべて
        </button>
        <button
          type="button"
          class="type-chip-btn"
          v-for="(category,index) in types"
          :key="index"
          :class="{ 'type-selected': selectedCategoryId === category.id }"          @click="handleCategoryClick(category)">
          {{ displayTypeName(category.name) }}
        </button>
      </div>
    </div>

    <!-- 商品卡片列表 -->
    <div>
      <el-row :gutter="16" v-if="goods.length > 0">
        <el-col :xs="12" :sm="8" :md="6" v-for="(item,index) in goods" :key="index" style="margin-top: 16px">
          <el-card :body-style="{ padding: '0px' }" class="card-item">
            <div class="goods-image-box">
              <img :src="getImageUrl(item.cover)" @error="handleImageError" alt="" class="goods-image">
              <span class="goods-status-badge" :class="item.state === '上架' ? 'status-active' : 'status-inactive'">
                {{ item.state || '上架' }}
              </span>
            </div>
            <div class="goods-info-body">
              <div class="goods-name-text" :title="item.name">
                {{item.name}}
              </div>
              <div class="goods-descr-text" :title="stripHtml(item.content || item.descr)">
                {{ stripHtml(item.content || item.descr) || '商品説明なし' }}
              </div>
              <div class="goods-price-row">
                <div class="goods-price-value">
                  {{item.price}}円
                </div>
                <div class="goods-store-text">
                  在庫: {{ item.store != null ? item.store : 1 }}点
                </div>
              </div>
              <div class="goods-publisher-tag" :title="item.userName || '匿名ユーザー'">
                <i class="el-icon-user"></i> 出品者：{{ item.userName || '匿名ユーザー' }}
              </div>
              <div class="goods-card-actions">
                <el-button
                    type="primary"
                    plain
                    size="mini"
                    class="action-btn edit-btn"
                    icon="el-icon-edit"
                    @click.stop="handleEdit(item)">
                  編集
                </el-button>
                <el-button
                    type="danger"
                    plain
                    size="mini"
                    class="action-btn delete-btn"
                    icon="el-icon-delete"
                    @click.stop="deleteGoods(item.id)">
                  削除
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <div v-if="total > 0" style="margin-top: 20px; text-align: right;">
        <el-pagination
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[8, 16, 32]"
            :page-size="pageSize"
            layout="total, prev, pager, next, jumper"
            :total="total"
            background
        ></el-pagination>
      </div>
    </div>

    <div v-if="goods.length == 0">
      <el-empty :image-size="200" :image="require('@/assets/empty.svg')" description="商品がありません"></el-empty>
    </div>

    <!-- 管理员编辑商品信息弹窗 -->
    <el-dialog
      title="商品情報編集"
      :visible.sync="editDialogVisible"
      width="560px"
      :close-on-click-modal="false"
      class="manager-goods-dialog"
    >
      <el-form
        ref="goodsFormRef"
        :model="goodsForm"
        :rules="goodsRules"
        label-width="95px"
        size="small"
      >
        <el-form-item label="出品者">
          <el-tag size="small" type="info" effect="plain">
            <i class="el-icon-user"></i> {{ goodsForm.userName || '匿名ユーザー' }} (ID: {{ goodsForm.userId || '-' }})
          </el-tag>
        </el-form-item>

        <el-form-item label="商品名" prop="name">
          <el-input
            v-model="goodsForm.name"
            placeholder="商品名を入力してください"
            maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="カテゴリ" prop="typeId">
          <el-select v-model="goodsForm.typeId" placeholder="カテゴリを選択" style="width: 100%">
            <el-option
              v-for="type in types"
              :key="type.id"
              :label="displayTypeName(type.name)"
              :value="type.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="価格 (円)" prop="price">
              <el-input-number
                v-model="goodsForm.price"
                :min="0.01"
                :precision="2"
                :step="1"
                controls-position="right"
                style="width: 100%"
              ></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="在庫数" prop="store">
              <el-input-number
                v-model="goodsForm.store"
                :min="1"
                :step="1"
                controls-position="right"
                style="width: 100%"
              ></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="ステータス" prop="state">
          <el-radio-group v-model="goodsForm.state">
            <el-radio label="上架">上架（公開・販売中）</el-radio>
            <el-radio label="下架">下架（非公開）</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="商品画像" prop="cover">
          <div class="cover-uploader-wrap">
            <el-upload
              action=""
              :http-request="uploadCover"
              :show-file-list="false"
              accept="image/*"
              class="cover-uploader"
            >
              <div v-if="goodsForm.cover" class="cover-preview-box">
                <img :src="getImageUrl(goodsForm.cover)" class="cover-preview-img" />
                <div class="cover-preview-mask">
                  <i class="el-icon-camera"></i>
                  <span>画像を変更</span>
                </div>
              </div>
              <div v-else class="cover-upload-placeholder">
                <i class="el-icon-upload"></i>
                <div>画像を選択</div>
              </div>
            </el-upload>
            <div class="cover-tip">※ JPG, PNG, WEBP形式対応</div>
          </div>
        </el-form-item>

        <el-form-item label="商品説明" prop="descr">
          <el-input
            type="textarea"
            :rows="3"
            v-model="goodsForm.descr"
            maxlength="1000"
            show-word-limit
            placeholder="商品の状態や特徴を入力"
          ></el-input>
        </el-form-item>

        <el-form-item label="注意事項" prop="purchaseNotice">
          <el-input
            type="textarea"
            :rows="2"
            v-model="goodsForm.purchaseNotice"
            maxlength="500"
            show-word-limit
            placeholder="受取方法や取引時の注意事項など"
          ></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">キャンセル</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">変更を保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "Goods",
  data() {
    return {
      types: [],
      selectedCategoryId: parseInt(this.$route.query.selectedCategoryId) || 0,
      total: 0,
      pageNum: 1,
      pageSize: 8,
      keyboard: '',
      goods: [],
      editDialogVisible: false,
      saving: false,
      goodsForm: {
        id: null,
        name: '',
        typeId: null,
        price: 0,
        store: 1,
        state: '上架',
        cover: '',
        descr: '',
        purchaseNotice: '',
        userName: '',
        userId: null
      },
      goodsRules: {
        name: [
          { required: true, message: '商品名を入力してください', trigger: 'blur' }
        ],
        typeId: [
          { required: true, message: 'カテゴリを選択してください', trigger: 'change' }
        ],
        price: [
          { required: true, message: '価格を入力してください', trigger: 'blur' }
        ],
        store: [
          { required: true, message: '在庫数を入力してください', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadType()
    this.loadGoods()
  },
  methods: {
    getImageUrl(url) {
      if (!url) return require('@/assets/empty.svg')
      if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
        return url
      }
      return this.$baseUrl + (url.startsWith('/') ? url : '/' + url)
    },
    handleImageError(e) {
      e.target.src = require('@/assets/empty.svg')
    },
    displayTypeName(name) {
      if (!name) return ''
      const categoryMap = {
        '零食': 'お菓子・食品',
        '饮料': '飲料・ドリンク',
        '数码产品': '家電・スマホ',
        '女装': 'レディース',
        '男装': 'メンズ',
        '家具': 'インテリア・家具',
        '办公用品': '文房具・日用品',
        '图书': '本・教科書',
        '美妆': 'コスメ・美容',
        '食品': 'お菓子・食品',
        '日用品': '文房具・日用品'
      }
      if (categoryMap[name]) return categoryMap[name]
      const key = 'category.' + name
      const translated = this.$t(key)
      return translated === key ? name : translated
    },
    loadType() {
      this.$request.get('/type/selectAll').then(res => {
        this.types = Array.isArray(res.data) ? res.data : []
      })
    },
    loadGoods() {
      this.$request.get("/goods/selectPage/type", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.keyboard,
          typeId: this.selectedCategoryId
        }
      }).then(res => {
        this.goods = res.data?.records || []
        this.total = res.data?.total || 0
      })
    },
    handleAllClick() {
      this.selectedCategoryId = 0
      this.$router.replace({
        query: { ...this.$route.query, selectedCategoryId: 0 }
      })
      this.loadGoods()
    },
    handleCategoryClick(category) {
      this.selectedCategoryId = category.id
      this.$router.replace({
        query: { ...this.$route.query, selectedCategoryId: category.id }
      })
      this.loadGoods()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.loadGoods()
    },
    handleEdit(item) {
      this.goodsForm = {
        id: item.id,
        name: item.name,
        typeId: item.typeId,
        price: parseFloat(item.price) || 0,
        store: parseInt(item.store != null ? item.store : 1) || 1,
        state: item.state || '上架',
        cover: item.cover || '',
        descr: item.descr || item.content || '',
        purchaseNotice: item.purchaseNotice || '',
        userName: item.userName || '',
        userId: item.userId || null
      }
      this.editDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.goodsFormRef) {
          this.$refs.goodsFormRef.clearValidate()
        }
      })
    },
    uploadCover(options) {
      const formData = new FormData()
      formData.append('file', options.file)
      this.$request.post('/file/upload', formData).then(res => {
        if (res.code === '200') {
          this.$set(this.goodsForm, 'cover', res.data)
          this.$message.success('画像をアップロードしました')
        } else {
          this.$message.error(res.msg || '画像のアップロードに失敗しました')
        }
      }).catch(() => {
        this.$message.error('画像のアップロードに失敗しました')
      })
    },
    submitEdit() {
      this.$refs.goodsFormRef.validate(valid => {
        if (valid) {
          if (!this.goodsForm.cover) {
            this.$message.warning('商品画像をアップロードしてください')
            return
          }
          this.saving = true
          this.$request.put('/goods/update', this.goodsForm).then(res => {
            this.saving = false
            if (res.code === '200') {
              this.$notify.success({
                title: '完了',
                message: '商品情報を更新しました',
                showClose: false,
                duration: 2000
              })
              this.editDialogVisible = false
              this.loadGoods()
            } else {
              this.$notify.error({
                title: 'エラー',
                message: res.msg || '更新に失敗しました',
                showClose: false,
                duration: 2000
              })
            }
          }).catch(err => {
            this.saving = false
            this.$notify.error({
              title: 'エラー',
              message: err.response?.data?.msg || '更新に失敗しました',
              showClose: false,
              duration: 2000
            })
          })
        }
      })
    },
    deleteGoods(id) {
      this.$confirm('この商品を削除してもよろしいですか？', '削除確認', {
        type: 'warning'
      }).then(() => {
        this.$request.delete('/goods/delete?id=' + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({
              title: '完了',
              message: '商品を削除しました',
              showClose: false,
              duration: 2000
            })
            this.loadGoods()
          } else {
            this.$notify.error({
              title: 'エラー',
              message: res.msg,
              showClose: false,
              duration: 2000
            })
          }
        })
      }).catch(() => {})
    },
    stripHtml(value) {
      const container = document.createElement('div')
      container.innerHTML = value || ''
      return (container.textContent || container.innerText || '').replace(/\s+/g, ' ').trim()
    }
  }
}
</script>

<style scoped>
.manager-goods-page {
  padding: 8px 4px;
  min-height: 85vh;
}

.goods-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-title {
  border-left: 5px solid #ff8a3d;
  padding-left: 10px;
  font-size: 20px;
  color: #1e293b;
  margin: 0;
}

.search-box-wrap {
  display: flex;
  align-items: center;
}

.search-input {
  width: 220px;
  padding: 10px 14px;
  outline: none;
  border: 1px solid #fed7aa;
  border-radius: 20px 0 0 20px;
  font-size: 13px;
  box-sizing: border-box;
  background: #fff;
  transition: border-color .2s ease;
}

.search-input:focus {
  border-color: #ff8a3d;
}

.search-button {
  padding: 10px 16px;
  background: linear-gradient(135deg, #ffa86b 0%, #ff7e29 100%);
  border: none;
  border-radius: 0 20px 20px 0;
  color: #fff;
  cursor: pointer;
}

.search-button:hover {
  opacity: .92;
}

.category-filter-area {
  margin-top: 16px;
}

.type-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

/* 分类小模块去掉背景色 */
.type-chip-btn {
  background: transparent !important;
  background-color: transparent !important;
  border: 1px solid #fed7aa;
  color: #64748b;
  border-radius: 18px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  outline: none;
  box-shadow: none !important;
  transition: all .2s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.type-chip-btn:hover {
  background: transparent !important;
  border-color: #ff8a3d;
  color: #ff8a3d;
  transform: translateY(-1px);
}

.type-chip-btn.type-selected {
  background: transparent !important;
  border: 2px solid #ff8a3d !important;
  color: #ea6b1f !important;
  font-weight: 700 !important;
}

.card-item {
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 4px 16px rgba(15, 23, 42, .03);
  transition: transform .25s ease, box-shadow .25s ease;
  overflow: hidden;
  background: #ffffff;
}

.card-item:hover {
  cursor: pointer;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(255, 126, 41, .15);
}

/* 等比缩小展示完整商品图片，绝不裁剪 */
.goods-image-box {
  width: 100%;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  overflow: hidden;
  border-bottom: 1px solid #f1f5f9;
  position: relative;
  padding: 8px;
  box-sizing: border-box;
}

.goods-image {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  object-fit: contain !important;
  display: block;
}

.goods-status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}

.status-active {
  background: rgba(16, 185, 129, 0.12);
  color: #059669;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.status-inactive {
  background: rgba(100, 116, 139, 0.12);
  color: #64748b;
  border: 1px solid rgba(100, 116, 139, 0.3);
}

.goods-info-body {
  padding: 12px;
}

.goods-name-text {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-descr-text {
  margin-top: 5px;
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.goods-price-value {
  font-size: 18px;
  color: #ff7e29;
  font-weight: 700;
}

.goods-store-text {
  font-size: 12px;
  color: #64748b;
}

.goods-publisher-tag {
  margin-top: 6px;
  color: #64748b;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-card-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.action-btn {
  flex: 1;
  border-radius: 6px;
  margin: 0 !important;
  font-weight: 500;
}

/* 弹窗图片上传预览 */
.cover-uploader-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cover-preview-box {
  width: 140px;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  position: relative;
  cursor: pointer;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  box-sizing: border-box;
}

.cover-preview-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.cover-preview-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  gap: 4px;
  opacity: 0;
  transition: opacity .2s ease;
}

.cover-preview-box:hover .cover-preview-mask {
  opacity: 1;
}

.cover-upload-placeholder {
  width: 140px;
  height: 140px;
  border: 2px dashed #fed7aa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ff8a3d;
  background: #fffaf5;
  cursor: pointer;
  transition: border-color .2s;
}

.cover-upload-placeholder:hover {
  border-color: #ff7e29;
}

.cover-upload-placeholder i {
  font-size: 28px;
  margin-bottom: 6px;
}

.cover-tip {
  font-size: 11px;
  color: #94a3b8;
}
</style>
