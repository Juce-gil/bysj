$ErrorActionPreference='Stop'
$ProgressPreference='SilentlyContinue'

function PostJson($url, $body, $token) {
  $headers = @{}
  if ($token) { $headers['Authorization'] = "Bearer $token" }
  Invoke-RestMethod -Uri $url -Method Post -ContentType 'application/json' -Body ($body | ConvertTo-Json -Depth 10) -Headers $headers
}
function PutJson($url, $body, $token) {
  $headers = @{}
  if ($token) { $headers['Authorization'] = "Bearer $token" }
  Invoke-RestMethod -Uri $url -Method Put -ContentType 'application/json' -Body ($body | ConvertTo-Json -Depth 10) -Headers $headers
}
function GetApi($url, $token) {
  $headers = @{}
  if ($token) { $headers['Authorization'] = "Bearer $token" }
  Invoke-RestMethod -Uri $url -Method Get -Headers $headers
}
function DeleteApi($url, $token) {
  $headers = @{}
  if ($token) { $headers['Authorization'] = "Bearer $token" }
  Invoke-RestMethod -Uri $url -Method Delete -Headers $headers
}
function PutNoBody($url, $token) {
  $headers = @{}
  if ($token) { $headers['Authorization'] = "Bearer $token" }
  Invoke-RestMethod -Uri $url -Method Put -Headers $headers
}
function UploadImages($url, $filePath, $token) {
  $out = & curl.exe -s -X POST -H "Authorization: Bearer $token" -F "files=@$filePath" $url
  if (-not $out) { throw 'upload returned empty response' }
  return $out | ConvertFrom-Json
}

$base='http://127.0.0.1:8080/api'
$ts=[DateTimeOffset]::Now.ToUnixTimeSeconds()
$regUser="check$ts"
$results=[System.Collections.Generic.List[string]]::new()

try {
  try {
    $null = PostJson "$base/auth/register" @{ username=$regUser; nickname='验收用户'; password='123456'; confirmPassword='123456' } $null
    $results.Add("注册: OK user=$regUser")
  } catch {
    $results.Add("注册: FAIL -> $($_.Exception.Message)")
  }

  $user1 = PostJson "$base/auth/login" @{ username='zhangsan'; password='123456' } $null
  $user1Token = $user1.data.token
  $results.Add('用户登录(zhangsan): OK')

  $user2 = PostJson "$base/auth/login" @{ username='lisi'; password='123456' } $null
  $user2Token = $user2.data.token
  $results.Add('用户登录(lisi): OK')

  $admin = PostJson "$base/auth/login" @{ username='admin'; password='123456' } $null
  $adminToken = $admin.data.token
  $results.Add('管理员登录(admin): OK')

  $cats = GetApi "$base/categories" $null
  $cat = [string]$cats.data[0].name
  $results.Add("分类列表: OK first=$cat")

  $null = GetApi "$base/users/me" $user1Token
  $results.Add('获取我的资料: OK')

  $null = PutJson "$base/users/me" @{ name='张三'; phone='13800000001'; qq='123456789'; slogan='验收阶段更新资料' } $user1Token
  $results.Add('更新我的资料: OK')

  $upload = UploadImages "$base/uploads/goods-images" 'E:\毕设\1.jpg' $user1Token
  $imgUrl = [string]$upload.data[0].url
  $results.Add("上传图片: OK url=$imgUrl")

  $goodsCreate = PostJson "$base/goods" @{ title="验收商品$ts"; price=66.6; originalPrice=88.8; category=$cat; campus='科成主校区'; condition='九成新'; intro='验收商品简介'; description='用于最终联调验收的测试商品'; tags=@('验收','测试'); imageUrls=@($imgUrl) } $user1Token
  $goodsId = [int64]$goodsCreate.data.id
  $results.Add("发布商品: OK id=$goodsId")

  $null = GetApi "$base/goods/$goodsId" $null
  $results.Add('商品详情: OK')

  $null = PostJson "$base/goods/$goodsId/favorite" @{} $user2Token
  $results.Add('收藏商品(用户2): OK')

  $null = PostJson "$base/comments" @{ goodsId=$goodsId; content='这是验收评论' } $user2Token
  $results.Add('发表评论(用户2): OK')

  $appt = PostJson "$base/appointments" @{ goodsId=$goodsId; intendedTime='2026-04-10 10:00:00'; intendedLocation='科成主校区东门'; remark='验收预约' } $user2Token
  $apptId = [int64]$appt.data.id
  $results.Add("发起预约(用户2): OK id=$apptId")

  $null = GetApi "$base/appointments/my" $user2Token
  $results.Add('我的预约列表(用户2): OK')

  $notifications = GetApi "$base/notifications" $user1Token
  $notifId = [int64]$notifications.data[0].id
  $results.Add('通知列表(用户1): OK')

  $unread = GetApi "$base/notifications/unread-count" $user1Token
  $results.Add("未读通知数(用户1): OK count=$($unread.data)")

  $null = PutNoBody "$base/notifications/$notifId/read" $user1Token
  $results.Add('通知标记已读(用户1): OK')

  $null = GetApi "$base/users/me/goods" $user1Token
  $results.Add('我的商品列表(用户1): OK')

  $null = PutJson "$base/goods/$goodsId" @{ title="验收商品$ts-改"; price=68.8; originalPrice=88.8; category=$cat; campus='科成主校区'; condition='九成新'; intro='验收商品简介已更新'; description='用于最终联调验收的测试商品，已更新'; tags=@('验收','测试','更新'); imageUrls=@($imgUrl) } $user1Token
  $results.Add('编辑商品: OK')

  $null = PutNoBody "$base/goods/$goodsId/off-shelf" $user1Token
  $results.Add('商品下架: OK')

  $null = PutNoBody "$base/goods/$goodsId/relist" $user1Token
  $results.Add('商品重新上架: OK')

  $wantedCreate = PostJson "$base/wanted" @{ title="验收求购$ts"; budget='预算 50 元左右'; category=$cat; campus='科成主校区'; deadline='2026-04-30'; intro='验收求购简介'; description='用于最终联调验收的测试求购'; tags=@('验收','求购'); imageUrls=@($imgUrl) } $user1Token
  $wantedId = [int64]$wantedCreate.data.id
  $results.Add("发布求购: OK id=$wantedId")

  $null = GetApi "$base/wanted/$wantedId" $user2Token
  $results.Add('求购详情: OK')

  $null = GetApi "$base/users/me/wanted" $user1Token
  $results.Add('我的求购列表: OK')

  $null = PutJson "$base/wanted/$wantedId" @{ title="验收求购$ts-改"; budget='预算 60 元左右'; category=$cat; campus='科成主校区'; deadline='2026-05-01'; intro='验收求购简介已更新'; description='用于最终联调验收的测试求购，已更新'; tags=@('验收','求购','更新'); imageUrls=@($imgUrl) } $user1Token
  $results.Add('编辑求购: OK')

  $null = PutNoBody "$base/wanted/$wantedId/close" $user1Token
  $results.Add('关闭求购: OK')

  $null = PutNoBody "$base/wanted/$wantedId/reopen" $user1Token
  $results.Add('重新开放求购: OK')

  $null = GetApi "$base/favorites" $user2Token
  $results.Add('我的收藏列表(用户2): OK')

  $null = GetApi "$base/admin/users?pageNum=1&pageSize=10" $adminToken
  $results.Add('管理员用户列表: OK')

  $null = GetApi "$base/admin/goods?pageNum=1&pageSize=10" $adminToken
  $results.Add('管理员商品列表: OK')

  $null = GetApi "$base/admin/announcements?pageNum=1&pageSize=10" $adminToken
  $results.Add('管理员公告列表: OK')

  $null = PutNoBody "$base/appointments/$apptId/cancel" $user2Token
  $results.Add('取消预约: OK')

  $null = DeleteApi "$base/wanted/$wantedId" $user1Token
  $results.Add('删除求购: OK')

  $null = DeleteApi "$base/goods/$goodsId" $user1Token
  $results.Add('删除商品: OK')

  $results | ForEach-Object { $_ }
}
catch {
  Write-Output '=== 已完成步骤 ==='
  $results | ForEach-Object { $_ }
  Write-Output '=== 当前失败 ==='
  Write-Output $_.Exception.Message
  if ($_.ErrorDetails -and $_.ErrorDetails.Message) { Write-Output $_.ErrorDetails.Message }
  exit 1
}
