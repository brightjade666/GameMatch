// script.js

// ----- 注入动画 & 弹窗样式 -----
(function addStyles() {
    const style = document.createElement('style');
    style.textContent = `
        @keyframes floatIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .msg-animate { animation: floatIn 0.4s ease-out; }
        .msg-section { flex: 1; min-width: 0; }
        .msg-section h4 { margin-top: 0; padding-bottom: 6px; border-bottom: 1px solid #eee; }
        .msg-item { animation: floatIn 0.4s ease-out; padding: 12px; border-bottom: 1px solid #eee; margin-bottom: 8px; background: #fafafa; border-radius: 6px; position: relative; }
        .view-btn { background: #0078d4; color: white; border: none; border-radius: 4px; padding: 4px 8px; font-size: 11px; cursor: pointer; margin-left: 8px; }
        .view-btn:hover { background: #005a9e; }
        .profile-modal {
            display: none; position: fixed; top:0; left:0; width:100%; height:100%; background: rgba(0,0,0,0.5); z-index:2000; align-items: center; justify-content: center;
        }
        .profile-modal.show { display: flex; }
        .profile-content {
            background: white; border-radius: 16px; padding: 30px; width: 400px; box-shadow: 0 10px 30px rgba(0,0,0,0.2); position: relative;
        }
        .profile-content img { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; }
        .profile-row { margin-bottom: 15px; }
        .profile-row label { font-size: 13px; color: #666; }
        .profile-row span { font-size: 15px; color: #333; margin-left: 8px; word-break: break-all; }
        .close-profile { position: absolute; top: 10px; right: 15px; font-size: 24px; cursor: pointer; color: #999; }
        .profile-modal { display: none; position: fixed; top:0; left:0; width:100%; height:100%; background: rgba(0,0,0,0.5); z-index:2000; align-items: center; justify-content: center; }
        .profile-modal.show { display: flex; }
    `;
    document.head.appendChild(style);

    const modal = document.createElement('div');
    modal.id = 'profileModal';
    modal.className = 'profile-modal';
    modal.innerHTML = `
        <div class="profile-content">
            <span class="close-profile" onclick="closeProfile()">×</span>
            <div style="text-align:center;margin-bottom:20px;">
                <img id="profileAvatar" src="">
                <h3 id="profileName" style="margin:10px 0 5px;"></h3>
            </div>
            <div class="profile-row"><label>性别：</label><span id="profileGender"></span></div>
            <div class="profile-row"><label>简介：</label><span id="profileBio"></span></div>
            <div class="profile-row"><label>常玩游戏：</label><span id="profileGame"></span></div>
            <div class="profile-row"><label>匹配需求：</label><span id="profileNeed"></span></div>
        </div>
    `;
    document.body.appendChild(modal);
})();

// ========== 全局状态与工具函数 ==========
let userData = JSON.parse(localStorage.getItem('currentUser')) || {};

function showToast(msg, type = 'info') {
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.textContent = msg || '操作完成';
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('toast-hidden');
        setTimeout(() => toast.remove(), 400);
    }, 2500);
}

function showConfirm(message, onConfirm) {
    document.getElementById('globalConfirmMsg').innerText = message;
    document.getElementById('globalConfirmModal').classList.add('active');
    const okBtn = document.getElementById('globalConfirmOk');
    const newBtn = okBtn.cloneNode(true);
    okBtn.parentNode.replaceChild(newBtn, okBtn);
    newBtn.addEventListener('click', () => {
        closeGlobalConfirm();
        onConfirm();
    });
}
function closeGlobalConfirm() {
    document.getElementById('globalConfirmModal').classList.remove('active');
}

// 登录状态检测
function isLoggedIn() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    return user && user.user_id;
}

function requireLogin() {
    if (!isLoggedIn()) {
        showToast("请先登录", "error");
        return false;
    }
    return true;
}

function goToIfLogin(url) {
    if (requireLogin()) {
        location.href = url;
    }
}

// 刷新导航栏用户信息（未登录时显示默认）
function updateNavUser() {
    const stored = JSON.parse(localStorage.getItem('currentUser')) || {};
    const avatarElem = document.getElementById('nav-avatar');
    const nickElem = document.getElementById('nav-nick');
    if (isLoggedIn()) {
        let avatar = stored.avatar || "";
        if (avatar && !avatar.startsWith("http")) {
            avatar = "http://localhost:8081" + avatar;
        }
        if (!avatar) avatar = "https://ui-avatars.com/api/?name=" + encodeURIComponent(stored.nick || stored.username || "用户");
        avatarElem.src = avatar;
        nickElem.innerText = stored.nick || stored.username || '用户';
    } else {
        avatarElem.src = "https://ui-avatars.com/api/?name=未登录";
        nickElem.innerText = "未登录";
    }
    const adminBtn = document.getElementById('n-admin');
    if (adminBtn) {
        const user = JSON.parse(localStorage.getItem('currentUser')) || {};
        adminBtn.style.display = (user.role === 9) ? 'inline' : 'none';
    }
}

// 查看用户资料弹窗
async function viewProfile(userId) {
    try {
        const res = await fetch(`http://localhost:8081/user/publicInfo?userId=${userId}`);
        const data = await res.json();
        if (data.code === 200) {
            document.getElementById('profileAvatar').src = data.avatar ? (data.avatar.startsWith('http') ? data.avatar : 'http://localhost:8081' + data.avatar) : 'https://ui-avatars.com/api/?name=' + encodeURIComponent(data.nick);
            document.getElementById('profileName').innerText = data.nick;
            document.getElementById('profileGender').innerText = data.gender;
            document.getElementById('profileBio').innerText = data.introduction || '无';
            document.getElementById('profileGame').innerText = data.gameName || '未设定';
            document.getElementById('profileNeed').innerText = data.matchNeed || '无';
            document.getElementById('profileModal').classList.add('show');
        } else {
            showToast('获取用户信息失败', 'error');
        }
    } catch (e) {
        showToast('网络错误', 'error');
    }
}
window.closeProfile = function () {
    document.getElementById('profileModal').classList.remove('show');
};

// ----- 主页渲染（带排序）-----
async function renderHome() {
    try {
        const user = JSON.parse(localStorage.getItem('currentUser')) || {};
        let url = "http://localhost:8081/team/list";
        if (user.user_id) {
            url += `?userId=${user.user_id}`;
        }
        const res = await fetch(url);
        const result = await res.json();
        const list = result.data || [];
        document.getElementById("home-list").innerHTML = list.map(t => `
            <div class="team-card" onclick="window.showDetail(${t.id})">
                <div class="card-img"><img src="http://localhost:8081${t.avatar}"></div>
                <div class="card-body">
                    <h3>${t.title}</h3>
                    <p><span class="btn-s" style="background:var(--danger)">队长</span> ${t.leader}</p>
                    <p><span class="btn-s" style="background:var(--success)">游戏</span> ${t.need}</p>
                </div>
            </div>
        `).join('');
    } catch (e) {
        console.error('加载主页失败', e);
    }
}

// ----- 队伍详情 & 评论（修改头像判断）-----
window.showDetail = async function (id) {
    if (!requireLogin()) return;
    const user = JSON.parse(localStorage.getItem('currentUser'));
    let joined = false;
    try {
        const r = await fetch(`http://localhost:8081/team/my/joined?userId=${user.user_id}`);
        const d = await r.json();
        joined = (d.data || []).some(t => t.teamId == id);
    } catch (e) { }

    const res = await fetch(`http://localhost:8081/team/detail/${id}`);
    const result = await res.json();
    const t = result.data;
    if (!t) return showToast("队伍不存在", "error");

    // 1. 反转评论数组：最早的在上面（后端是最新在前）
    const comments = (t.comments || []).slice().reverse();

    // 2. 生成评论 HTML（左右布局）
    const commentsHtml = comments.length ? comments.map(c => {
        const isMe = (c.userId == user.user_id);
        const cls = isMe ? 'self' : 'other';
        const avatarUrl = c.avatar && c.avatar.trim().length > 0
            ? 'http://localhost:8081' + c.avatar
            : 'https://ui-avatars.com/api/?name=' + encodeURIComponent(c.author);
        return `
        <div class="comment-item ${cls}">
            <img class="comment-avatar" src="${avatarUrl}" onerror="this.src='https://ui-avatars.com/api/?name='+encodeURIComponent('${c.author}')">
            <div class="comment-content">
                <div class="comment-header">
                    <span class="comment-author">${c.author}</span>
                    <span class="comment-time">${c.time}</span>
                </div>
                <p class="comment-text">${c.text.replace(/\n/g, '<br>')}</p>
            </div>
        </div>`;
    }).join('') : '<p style="color:#999">暂无留言</p>';

    // 3. 详情弹窗内容（修改“需求”为“游戏”）
    document.getElementById('detail-content').innerHTML = `
        <div class="detail-split">
            <img class="detail-cover" src="http://localhost:8081${t.avatar}">
            <div class="detail-info">
                <h2>${t.title}</h2>
                <p><strong>队长：</strong>${t.leader}</p>
                <p style="color:red"><strong>游戏：</strong>${t.need}</p>
                <div style="background:#f9f9f9;padding:10px;border-radius:8px">${t.desc}</div>
            </div>
        </div>
        <div class="comment-section">
            <h4>留言 (${t.comments?.length || 0})</h4>
            <div class="comment-list" id="comment-list-${t.id}">${commentsHtml}</div>
            <div class="comment-input-box">
                <textarea id="comment-input-${t.id}" placeholder="按 Enter 发送，Shift+Enter 换行" rows="1" style="flex:1; padding:8px; border-radius:20px; border:1px solid #ddd; resize:none;"></textarea>
                <button class="btn-s" style="background:var(--primary);padding:8px 16px; border-radius:20px;" onclick="sendComment(${t.id})">发送</button>
            </div>
        </div>
        ${joined ? '<p style="text-align:center;color:#999;margin-top:15px">你已是队员</p>'
            : `<button class="btn-s" style="background:var(--primary);width:100%;margin-top:15px" onclick="applyJoinTeam(${t.id})">申请加入</button>`}
    `;

    // 4. 自动滚动到留言底部（最新留言）
    setTimeout(() => {
        const listEl = document.getElementById(`comment-list-${t.id}`);
        if (listEl) listEl.scrollTop = listEl.scrollHeight;
    }, 50);

    // 5. 绑定评论输入框的 Enter 事件
    const commentInput = document.getElementById(`comment-input-${t.id}`);
    if (commentInput) {
        commentInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendComment(t.id);
            }
            autoResizeTextarea(commentInput);
        });
    }

    document.getElementById('detail-modal').style.display = 'block';
};

// 辅助：自动调整 textarea 高度
function autoResizeTextarea(el) {
    el.style.height = 'auto';
    el.style.height = el.scrollHeight + 'px';
}

// 发送评论
window.sendComment = async function (teamId) {
    if (!requireLogin()) return;
    const user = JSON.parse(localStorage.getItem('currentUser'));
    const input = document.getElementById(`comment-input-${teamId}`);
    if (!input) return;
    const content = input.value.trim();
    if (!content) return;

    try {
        const res = await fetch(`http://localhost:8081/team/comment?teamId=${teamId}&userId=${user.user_id}&content=${encodeURIComponent(content)}`, {
            method: 'POST'
        });
        const result = await res.json();
        if (result.code === 200) {
            input.value = '';
            autoResizeTextarea(input);
            // 刷新评论列表（按正序渲染）
            const detailRes = await fetch(`http://localhost:8081/team/detail/${teamId}`);
            const detail = await detailRes.json();
            if (detail.code === 200) {
                const comments = (detail.data.comments || []).slice().reverse(); // 反转，最早在上
                const listEl = document.getElementById(`comment-list-${teamId}`);
                if (listEl) {
                    listEl.innerHTML = comments.length ? comments.map(c => {
                        const isMe = (c.userId == user.user_id);
                        const cls = isMe ? 'self' : 'other';
                        const avatarUrl = c.avatar && c.avatar.trim().length > 0
                            ? 'http://localhost:8081' + c.avatar
                            : 'https://ui-avatars.com/api/?name=' + encodeURIComponent(c.author);
                        return `
                        <div class="comment-item ${cls}">
                            <img class="comment-avatar" src="${avatarUrl}" onerror="this.src='https://ui-avatars.com/api/?name='+encodeURIComponent('${c.author}')">
                            <div class="comment-content">
                                <div class="comment-header">
                                    <span class="comment-author">${c.author}</span>
                                    <span class="comment-time">${c.time}</span>
                                </div>
                                <p class="comment-text">${c.text.replace(/\n/g, '<br>')}</p>
                            </div>
                        </div>`;
                    }).join('') : '<p style="color:#999">暂无留言</p>';
                    // 滚动到底部
                    setTimeout(() => { listEl.scrollTop = listEl.scrollHeight; }, 50);
                }
            }
        } else {
            showToast(result.msg || '评论失败', 'error');
        }
    } catch (e) {
        showToast('网络错误', 'error');
    }
};

window.applyJoinTeam = async function (teamId) {
    if (!requireLogin()) return;
    const user = JSON.parse(localStorage.getItem('currentUser'));
    try {
        const r = await fetch(`http://localhost:8081/team/my/joined?userId=${user.user_id}`);
        const d = await r.json();
        if ((d.data || []).some(t => t.teamId == teamId)) {
            return showToast("你已加入该队伍", "error");
        }
    } catch (e) { }

    const btn = event?.target;
    if (btn && btn.disabled) return;
    if (btn) {
        btn.disabled = true;
        btn.textContent = '申请中...';
    }
    try {
        const res = await fetch("http://localhost:8081/team/apply", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ teamId, userId: user.user_id })
        });
        const result = await res.json();
        if (result.code === 200) {
            showToast(result.msg || '申请成功', 'success');
        } else {
            showToast(result.msg || '申请失败', 'error');
        }
    } catch (e) {
        showToast('网络错误', 'error');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.textContent = '申请加入';
        }
    }
};

window.closeDetail = function (e) {
    if (e.target === document.getElementById('detail-modal')) {
        document.getElementById('detail-modal').style.display = 'none';
    }
};

// ----- 页面切换 -----
window.switchP = function (p) {
    document.querySelectorAll('.nav-links span').forEach(s => s.classList.remove('active'));
    document.getElementById('n-' + p).classList.add('active');
    document.getElementById('p-home').style.display = p === 'home' ? 'block' : 'none';
    document.getElementById('p-profile').style.display = p === 'profile' ? 'block' : 'none';

    if (p === 'home') {
        renderHome();
    } else if (p === 'profile') {
        const stored = JSON.parse(localStorage.getItem('currentUser')) || {};
        if (isLoggedIn()) {
            userData = stored;
            let avatar = userData.avatar || "";
            if (avatar && !avatar.startsWith("http")) avatar = "http://localhost:8081" + avatar;
            if (!avatar) avatar = "https://ui-avatars.com/api/?name=" + encodeURIComponent(userData.nick || userData.username || "用户");
            document.getElementById('info-avatar').src = avatar;
            document.getElementById('info-nick').innerText = userData.nick || userData.username;
            document.getElementById('info-uid').innerText = userData.user_id || '';
            document.getElementById('info-bio').innerText = userData.introduction || '';
            document.getElementById('info-utime').innerText = userData.update_time || '';
        } else {
            document.getElementById('info-avatar').src = "https://ui-avatars.com/api/?name=未登录";
            document.getElementById('info-nick').innerText = "未登录";
            document.getElementById('info-uid').innerText = "——";
            document.getElementById('info-bio').innerText = "请先登录以查看个人信息";
            document.getElementById('info-utime').innerText = "";
        }
        switchTab('teams');
    }
};

// ----- 个人中心 Tab -----
window.switchTab = async function (type) {
    if (!requireLogin()) {
        document.getElementById('tab-content').innerHTML = "<p>请先登录后查看</p>";
        return;
    }
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active-tab'));
    if (document.getElementById('t-' + type)) {
        document.getElementById('t-' + type).classList.add('active-tab');
    }
    const tab = document.getElementById('tab-content');
    const user = JSON.parse(localStorage.getItem('currentUser'));

    if (type === 'teams') {
        try {
            const res = await fetch(`http://localhost:8081/team/my/joined?userId=${user.user_id}`);
            const result = await res.json();
            const list = result.data || [];
            if (!list.length) { tab.innerHTML = "<p>你还没有加入任何队伍</p>"; return; }
            let html = "";
            list.forEach(team => {
                const isLeader = team.leaderId == user.user_id;
                const leaderDetail = team.memberDetails?.find(d => d.userId == team.leaderId);
                const leaderName = leaderDetail ? leaderDetail.nick : '未知';

                html += `<div style="border:1px solid #eee;border-radius:10px;padding:15px;margin-bottom:12px" class="msg-animate">
                    <h3>${team.teamName} ${isLeader ? '<span style="color:var(--primary);font-size:12px">(队长)</span>' : ''}</h3>
                    <p>队长：${leaderName}</p>
                    <p>加入时间：${new Date(team.joinTime).toLocaleString()}</p>
                    <hr><h4>队员 (${team.memberDetails.length}人)</h4>`;

                team.memberDetails.forEach(m => {
                    const isMe = m.userId == user.user_id;
                    const displayName = m.nick + (isMe ? ' <span style="color:#999;font-size:12px">(我)</span>' : '');
                    html += `<div style="padding:5px 0">${displayName} ｜ ${m.joinTime}</div>`;
                });

                html += (isLeader ? `<button class="btn-s" style="background:#dc3545;margin-top:10px" onclick="dissolveTeam(${team.teamId})">解散队伍</button>`
                    : `<button class="btn-s" style="background:#dc3545;margin-top:10px" onclick="leaveTeam(${team.teamId})">退出队伍</button>`);
                html += `</div>`;
            });
            tab.innerHTML = html;
        } catch (e) { tab.innerHTML = "<p>加载失败</p>"; }
    } else if (type === 'msgs') {
        try {
            const notiRes = await fetch(`http://localhost:8081/notification/my?userId=${user.user_id}`);
            const allNoti = (await notiRes.json()).data || [];

            const applyRes = await fetch(`http://localhost:8081/team/my/applies?leaderId=${user.user_id}`);
            let applyData = (await applyRes.json()).data || [];
            applyData.sort((a, b) => new Date(b.applyTime) - new Date(a.applyTime));

            const friendApplyRes = await fetch(`http://localhost:8081/friend/applies/received?userId=${user.user_id}`);
            const friendApplies = (await friendApplyRes.json()).data || [];

            const teamNoti = allNoti.filter(n => ['dissolve', 'leave', 'join', 'kick'].includes(n.type));
            const friendNoti = allNoti.filter(n => ['friend_accept', 'friend_delete'].includes(n.type));

            let notiHtml = '<div class="msg-section"><h4>队伍消息</h4>';
            if (teamNoti.length) {
                teamNoti.forEach(n => {
                    notiHtml += `<div class="msg-item">
                    <p>${n.content}</p>
                    <span style="font-size:12px;color:#999">${new Date(n.createTime).toLocaleString()}</span>
                </div>`;
                });
            } else {
                notiHtml += '<div class="msg-item" style="color:#999;text-align:center">暂无消息</div>';
            }
            notiHtml += '</div>';

            let applyHtml = '<div class="msg-section"><h4>入队申请</h4>';
            if (applyData.length) {
                applyData.forEach(a => {
                    applyHtml += `<div class="msg-item">
                    <p>申请人：${a.applierName || '用户' + a.userId} 申请加入「${a.teamName}」</p>
                    <p>申请时间：${new Date(a.applyTime).toLocaleString()}</p>
                    <p>状态：${a.status === 0 ? '待处理' : a.status === 1 ? '已同意' : '已拒绝'}</p>
                    <button class="view-btn" onclick="viewProfile(${a.userId})">查看资料</button>
                    ${a.status === 0 ? `
                    <div style="display:flex;gap:10px;margin-top:8px">
                        <button class="btn-s" style="background:var(--success);flex:1" onclick="agreeApply(${a.id})">同意</button>
                        <button class="btn-s" style="background:var(--danger);flex:1" onclick="rejectApply(${a.id})">拒绝</button>
                    </div>` : ''}
                </div>`;
                });
            } else {
                applyHtml += '<div class="msg-item" style="color:#999;text-align:center">暂无申请</div>';
            }
            applyHtml += '</div>';

            let friendHtml = '<div class="msg-section"><h4>好友信息</h4>';
            if (friendApplies.length) {
                friendApplies.forEach(a => {
                    const fullReason = a.reason || '无';
                    const shortReason = fullReason.length > 20 ? fullReason.substring(0, 20) + '...' : fullReason;
                    const reasonHtml = fullReason.length > 20
                        ? `<span>${shortReason}</span> <button class="view-btn" data-reason="${fullReason.replace(/"/g, '&quot;').replace(/'/g, '&#39;')}">详情</button>`
                        : `<span>${fullReason}</span>`;

                    friendHtml += `<div class="msg-item">
        <p>${a.fromName} 申请加为好友</p>
        <p>原因：${reasonHtml}</p>
        <button class="view-btn" onclick="viewProfile(${a.fromUserId})">查看资料</button>
        <div style="display:flex;gap:10px;margin-top:8px">
            <button class="btn-s" style="background:var(--success);flex:1" onclick="handleFriendApply(${a.id},'accept')">同意</button>
            <button class="btn-s" style="background:var(--danger);flex:1" onclick="handleFriendApply(${a.id},'reject')">拒绝</button>
        </div>
    </div>`;
                });
            }
            if (friendNoti.length) {
                friendNoti.forEach(n => {
                    friendHtml += `<div class="msg-item">
                    <p>${n.content}</p>
                    <span style="font-size:12px;color:#999">${new Date(n.createTime).toLocaleString()}</span>
                </div>`;
                });
            }
            if (!friendApplies.length && !friendNoti.length) {
                friendHtml += '<div class="msg-item" style="color:#999;text-align:center">暂无好友信息</div>';
            }
            friendHtml += '</div>';

            tab.innerHTML = `<div style="display:flex; gap:20px;">${notiHtml}${applyHtml}${friendHtml}</div>`;
        } catch (e) { tab.innerHTML = "<p>加载失败</p>"; }
    } else if (type === 'friends') {
        try {
            const res = await fetch(`http://localhost:8081/friend/list?userId=${user.user_id}`);
            const friends = (await res.json()).data || [];
            if (!friends.length) { tab.innerHTML = "<p>你还没有好友</p>"; return; }
            let html = '<div style="display:flex; flex-direction:column; gap:12px;">';
            friends.forEach(f => {
                html += `<div style="display:flex; align-items:center; background:#fff; padding:12px; border-radius:10px; box-shadow:0 2px 8px rgba(0,0,0,0.05); gap:15px;">
                <img src="${f.avatar ? 'http://localhost:8081' + f.avatar : 'https://ui-avatars.com/api/?name=' + encodeURIComponent(f.name)}" style="width:48px;height:48px;border-radius:50%;object-fit:cover;">
                <span style="font-weight:500; flex:1;">${f.name}</span>
                <button class="btn-s" style="background:var(--primary);" onclick="viewProfile(${f.friendId})">查看资料</button>
                <button class="btn-s" style="background:var(--danger);" onclick="deleteFriend(${f.friendId})">删除好友</button>
            </div>`;
            });
            html += '</div>';
            tab.innerHTML = html;
        } catch (e) { tab.innerHTML = "<p>加载失败</p>"; }
    }
};

// ----- 好友申请处理 -----
async function handleFriendApply(applyId, action) {
    const res = await fetch(`http://localhost:8081/friend/handle?applyId=${applyId}&action=${action}`, { method: 'POST' });
    const r = await res.json();
    showToast(r.msg, 'success');
    switchTab('msgs');
}

// ----- 删除好友 -----
async function deleteFriend(friendId) {
    if (!requireLogin()) return;
    const user = JSON.parse(localStorage.getItem('currentUser'));
    showConfirm('确定删除该好友吗？', async () => {
        const res = await fetch(`http://localhost:8081/friend/delete?userId=${user.user_id}&friendId=${friendId}`, { method: 'DELETE' });
        const r = await res.json();
        showToast(r.msg, r.code === 200 ? 'success' : 'error');
        if (r.code === 200) switchTab('friends');
    });
}

// ----- 队伍操作 -----
async function dissolveTeam(teamId) {
    if (!requireLogin()) return;
    const user = JSON.parse(localStorage.getItem('currentUser'));
    showConfirm('确定解散该队伍吗？', async () => {
        const res = await fetch(`http://localhost:8081/team/dissolve?teamId=${teamId}&leaderId=${user.user_id}`, { method: 'POST' });
        const r = await res.json();
        showToast(r.msg || '操作完成', r.code === 200 ? 'success' : 'error');
        if (r.code === 200) switchTab('teams');
    });
}
async function leaveTeam(teamId) {
    if (!requireLogin()) return;
    const user = JSON.parse(localStorage.getItem('currentUser'));
    showConfirm('确定退出该队伍吗？', async () => {
        const res = await fetch(`http://localhost:8081/team/leave?teamId=${teamId}&userId=${user.user_id}`, { method: 'POST' });
        const r = await res.json();
        showToast(r.msg || '操作完成', r.code === 200 ? 'success' : 'error');
        if (r.code === 200) switchTab('teams');
    });
}
async function agreeApply(id) {
    const res = await fetch(`http://localhost:8081/team/agree?id=${id}`, { method: 'POST' });
    const r = await res.json();
    showToast(r.msg || '已同意', 'success');
    switchTab('msgs');
}
async function rejectApply(id) {
    const res = await fetch(`http://localhost:8081/team/reject?id=${id}`, { method: 'POST' });
    const r = await res.json();
    showToast(r.msg || '已拒绝', 'error');
    switchTab('msgs');
}

// 显示完整申请原因弹窗
function showReasonDetail(reason) {
    const overlay = document.createElement('div');
    overlay.className = 'profile-modal';
    overlay.style.display = 'flex';
    overlay.innerHTML = `
        <div class="profile-content" style="max-width:350px;">
            <span class="close-profile" onclick="this.parentElement.parentElement.remove()">×</span>
            <h4 style="margin-bottom:10px;">申请原因</h4>
            <p style="word-wrap:break-word; line-height:1.6;">${reason}</p>
        </div>
    `;
    document.body.appendChild(overlay);
}
function logout() {
    showConfirm('确定退出登录吗？', async () => {
        await GameMatchApi.logout();
        location.href = 'login.html';
    });
}

document.addEventListener('click', function (e) {
    if (e.target.classList.contains('view-btn') && e.target.dataset.reason) {
        showReasonDetail(e.target.dataset.reason);
    }
});

// ========== 初始化 ==========
async function init() {
    let stored = JSON.parse(localStorage.getItem('currentUser')) || {};
    if (stored.user_id) {
        try {
            const res = await fetch("http://localhost:8081/user/info?userId=" + stored.user_id);
            const result = await res.json();
            if (!result.data) {
                localStorage.removeItem('currentUser');
                stored = {};
            } else {
                // ✅ 合并原有数据，保留 role、token 等字段
                const merged = { ...stored, ...result.data };
                localStorage.setItem('currentUser', JSON.stringify(merged));
                userData = merged;
            }
        } catch (e) {
            localStorage.removeItem('currentUser');
            stored = {};
        }
    }

    updateNavUser();

    if (!isLoggedIn()) {
        document.getElementById('p-home').style.display = 'block';
        document.getElementById('p-profile').style.display = 'none';
        renderHome();
    } else {
        renderHome();
        if (localStorage.getItem('switchToProfile') === 'true') {
            localStorage.removeItem('switchToProfile');
            switchP('profile');
        }
    }
}

document.addEventListener('DOMContentLoaded', init);
