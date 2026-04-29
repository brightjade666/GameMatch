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
        .profile-row span { font-size: 15px; color: #333; margin-left: 8px; }
        .close-profile { position: absolute; top: 10px; right: 15px; font-size: 24px; cursor: pointer; color: #999; }
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
        // 未登录显示默认图标
        avatarElem.src = "https://ui-avatars.com/api/?name=未登录";
        nickElem.innerText = "未登录";
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

// ----- 主页渲染 -----
async function renderHome() {
    try {
        const res = await fetch("http://localhost:8081/team/list");
        const result = await res.json();
        const list = result.data || [];
        document.getElementById("home-list").innerHTML = list.map(t => `
            <div class="team-card" onclick="window.showDetail(${t.id})">
                <div class="card-img"><img src="http://localhost:8081${t.avatar}"></div>
                <div class="card-body">
                    <h3>${t.title}</h3>
                    <p><span class="btn-s" style="background:var(--danger)">队长</span> ${t.leader}</p>
                    <p><span class="btn-s" style="background:var(--success)">需求</span> ${t.need}</p>
                </div>
            </div>
        `).join('');
    } catch (e) {
        console.error('加载主页失败', e);
    }
}

// ----- 队伍详情 & 申请 -----
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

    const commentsHtml = t.comments?.length ? t.comments.map(c =>
        `<div class="comment-item">....</div>`).join('') : '<p style="color:#999">暂无留言</p>';

    document.getElementById('detail-content').innerHTML = `
        <div class="detail-split">
            <img class="detail-cover" src="http://localhost:8081${t.avatar}">
            <div class="detail-info">
                <h2>${t.title}</h2>
                <p><strong>队长：</strong>${t.leader}</p>
                <p style="color:red"><strong>需求：</strong>${t.need}</p>
                <div style="background:#f9f9f9;padding:10px;border-radius:8px">${t.desc}</div>
            </div>
        </div>
        <div class="comment-section">
            <h4>留言 (${t.comments?.length || 0})</h4>
            <div class="comment-list">${commentsHtml}</div>
            <div class="comment-input-box">
                <input placeholder="评论功能暂未开通"><button class="btn-s" style="background:var(--primary)">发送</button>
            </div>
        </div>
        ${joined ? '<p style="text-align:center;color:#999;margin-top:15px">你已是队员</p>'
            : `<button class="btn-s" style="background:var(--primary);width:100%;margin-top:15px" onclick="applyJoinTeam(${t.id})">申请加入</button>`}
    `;
    document.getElementById('detail-modal').style.display = 'block';
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

// ----- 页面切换（已修改）-----
window.switchP = function (p) {
    document.querySelectorAll('.nav-links span').forEach(s => s.classList.remove('active'));
    document.getElementById('n-' + p).classList.add('active');
    document.getElementById('p-home').style.display = p === 'home' ? 'block' : 'none';
    document.getElementById('p-profile').style.display = p === 'profile' ? 'block' : 'none';

    if (p === 'home') {
        renderHome();
    } else if (p === 'profile') {
        // 读取最新登录状态
        const stored = JSON.parse(localStorage.getItem('currentUser')) || {};
        if (isLoggedIn()) {
            // 已登录：展示真实信息（尝试从服务器刷新一次）
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
            // ----- 未登录状态显示 -----
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
        // 未登录则直接展示空白提示，不调用后端接口
        document.getElementById('tab-content').innerHTML = "<p>请先登录后查看</p>";
        return;
    }
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active-tab'));
    document.getElementById('t-' + type).classList.add('active-tab');
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
        // …（消息部分不变）
        try {
            const notiRes = await fetch(`http://localhost:8081/notification/my?userId=${user.user_id}`);
            const notiData = (await notiRes.json()).data || [];

            const applyRes = await fetch(`http://localhost:8081/team/my/applies?leaderId=${user.user_id}`);
            let applyData = (await applyRes.json()).data || [];
            applyData.sort((a, b) => new Date(b.applyTime) - new Date(a.applyTime));

            let notiHtml = '<div class="msg-section"><h4>队伍消息</h4>';
            if (notiData.length) {
                notiData.forEach(n => {
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

            tab.innerHTML = `<div style="display:flex; gap:20px;">${notiHtml}${applyHtml}</div>`;
        } catch (e) { tab.innerHTML = "<p>加载失败</p>"; }
    }
};

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

function logout() {
    showConfirm('确定退出登录吗？', () => {
        localStorage.clear();
        location.href = 'login.html';
    });
}
// ----- 初始化（核心修改：启动时校验合法性）-----
async function init() {
    let stored = JSON.parse(localStorage.getItem('currentUser')) || {};
    if (stored.user_id) {
        try {
            const res = await fetch("http://localhost:8081/user/info?userId=" + stored.user_id);
            const result = await res.json();
            if (!result.data) {
                // 服务器查无此人 → 清除本地登录态
                localStorage.removeItem('currentUser');
                stored = {};
            } else {
                // 更新本地存储为最新信息
                userData = result.data;
                localStorage.setItem('currentUser', JSON.stringify(userData));
            }
        } catch (e) {
            // 网络错误也视为未登录
            localStorage.removeItem('currentUser');
            stored = {};
        }
    }

    // 无论是否登录，都更新导航栏
    updateNavUser();

    // 如果最终未登录，强制展示主页（不进入个人中心）
    if (!isLoggedIn()) {
        document.getElementById('p-home').style.display = 'block';
        document.getElementById('p-profile').style.display = 'none';
        renderHome();
    } else {
        renderHome();
    }
}

document.addEventListener('DOMContentLoaded', init);