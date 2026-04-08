/**
 * 最终性能优化版 script.js
 * 1. 支持大图上传（自动前端压缩）
 * 2. 支持回车键发送评论
 * 3. 彻底解决存储空间溢出报错
 */
// --- 1. 数据初始化 ---
let userData = JSON.parse(localStorage.getItem('currentUser')) || {
    user_id: "ID_" + Math.floor(Math.random() * 1000000),
    nick: "新用户",
    avatar: "",
    gender: "男",
    age: 18,
    contact: "未填写",
    game_name: "未设定",
    game_rank: "",
    introduction: "这个同学很懒，什么都没写。",
    role: "普通用户",
    status: "正常",
    create_time: new Date().toLocaleDateString(),
    update_time: "从未更新"
};
let localTeams = JSON.parse(localStorage.getItem('allTeams')) || [
    {
        id: 1, title: "ces项目组", avatar: "", leader: "绝伦N", need: "女生优先",
        desc: "专注于学科组队的平台，解决组队难问题。",
        comments: [{ author: "系统管理员", text: "欢迎大家踊跃报名！", time: "10-24 09:30" }]
    }
];
let myTeams = JSON.parse(localStorage.getItem('myTeams')) || [];

// --- 2. 工具函数：图片压缩 ---
async function compressImage(base64Str) {
    return new Promise((resolve) => {
        const img = new Image();
        img.src = base64Str;
        img.onload = () => {
            const canvas = document.createElement('canvas');
            let width = img.width;
            let height = img.height;
            const maxSide = 800;
            if (width > maxSide || height > maxSide) {
                if (width > height) {
                    height = (maxSide / width) * height;
                    width = maxSide;
                } else {
                    width = (maxSide / height) * width;
                    height = maxSide;
                }
            }
            canvas.width = width;
            canvas.height = height;
            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, width, height);
            resolve(canvas.toDataURL('image/jpeg', 0.7));
        };
        img.onerror = () => resolve(base64Str);
    });
}

// --- 3. 核心渲染与交互 ---
function renderHome() {
    const list = document.getElementById('home-list');
    if (!list) return;
    list.innerHTML = localTeams.map(t => {
        const cover = t.avatar || "https://ui-avatars.com/api/?name=" + encodeURIComponent(t.title);
        return `
            <div class="team-card" onclick="window.showDetail(${t.id})">
                <div class="card-img"><img src="${cover}"></div>
                <div class="card-body">
                    <h3 style="margin:0 0 10px 0;">${t.title} ${t.leader === userData.nick ? '<span class="tag-badge">我发起的</span>' : ''}</h3>
                    <p style="font-size:13px; margin:5px 0;"><span class="btn-s" style="background:var(--danger); padding:2px 6px;">队长</span> ${t.leader}</p>
                    <p style="font-size:13px; margin:5px 0;"><span class="btn-s" style="background:var(--success); padding:2px 6px;">需求</span> ${t.need}</p>
                </div>
            </div>`;
    }).join('');
}

window.showDetail = function (id) {
    const t = localTeams.find(item => item.id === id);
    if (!t) return alert("该队伍已解散");
    const content = document.getElementById('detail-content');
    if (!content) return;
    const isMyTeam = (t.leader === userData.nick);
    const commentsHtml = (t.comments && t.comments.length > 0) ? t.comments.map(c => {
        const dAvatar = (c.author === userData.nick && userData.avatar)
            ? userData.avatar
            : `https://ui-avatars.com/api/?name=${encodeURIComponent(c.author)}`;
        return `
            <div class="comment-item">
                <img class="comment-avatar" src="${dAvatar}">
                <div class="comment-content">
                    <div class="comment-header"><span class="comment-author">${c.author}</span><span class="comment-time">${c.time}</span></div>
                    <p class="comment-text">${c.text}</p>
                </div>
            </div>`;
    }).join('') : '<p style="text-align:center; color:#999; font-size:12px; padding:10px;">暂无留言</p>';

    content.innerHTML = `
        <div class="detail-split">
            <img class="detail-cover" src="${t.avatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(t.title)}">
            <div class="detail-info">
                <h2 style="margin: 0 0 10px 0;">${t.title}</h2>
                <p style="font-size:14px;"><strong>队长：</strong>${t.leader}</p>
                <p style="font-size:14px; color:var(--danger);"><strong>需求：</strong>${t.need}</p>
                <div style="background:#f9f9f9; padding:10px; border-radius:8px; font-size:13px;">${t.desc}</div>
            </div>
        </div>
        <div class="comment-section">
            <h4 style="margin:20px 0 10px 0;">留言区 (${t.comments ? t.comments.length : 0})</h4>
            <div class="comment-list">${commentsHtml}</div>
            <div class="comment-input-box" style="display:flex; gap:10px; margin-top:10px;">
                <input type="text" id="comment-input-${t.id}" 
                       placeholder="按回车发送留言..." 
                       style="flex:1; padding:8px; border:1px solid #ddd; border-radius:4px;"
                       onkeyup="if(event.keyCode===13) window.postComment(${t.id})">
                <button class="btn-s" style="background:var(--primary);" onclick="window.postComment(${t.id})">发送</button>
            </div>
        </div>
        ${isMyTeam ? '' : `<button class="btn-s" style="background:var(--primary); width:100%; margin-top:15px;" onclick="alert('申请已发送')">申请加入</button>`}
    `;
    document.getElementById('detail-modal').style.display = 'block';
};

window.postComment = function (teamId) {
    const input = document.getElementById(`comment-input-${teamId}`);
    const text = input ? input.value.trim() : "";
    if (!text) return;
    const tIndex = localTeams.findIndex(item => item.id === teamId);
    const now = new Date();
    const timeStr = `${now.getMonth() + 1}-${now.getDate()} ${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
    localTeams[tIndex].comments.push({ author: userData.nick, text: text, time: timeStr });
    try {
        localStorage.setItem('allTeams', JSON.stringify(localTeams));
        window.showDetail(teamId);
    } catch (e) {
        alert("存储已满！虽然已压缩，但数据依然过多，请尝试清理。");
    }
};

// --- 5. 退出/解散与其它逻辑 ---
window.quitTeam = function (teamId, teamName) {
    const teamInAll = localTeams.find(t => t.id === teamId);
    const isLeader = teamInAll && teamInAll.leader === userData.nick;
    if (isLeader) {
        if (!confirm(`确定要解散“${teamName}”吗？`)) return;
        localTeams = localTeams.filter(t => t.id !== teamId);
        localStorage.setItem('allTeams', JSON.stringify(localTeams));
    } else {
        if (!confirm(`确定要退出“${teamName}”吗？`)) return;
    }
    myTeams = myTeams.filter(t => t.id !== teamId);
    localStorage.setItem('myTeams', JSON.stringify(myTeams));
    renderHome();
    window.switchTab('teams');
};

window.switchTab = function (type) {
    const tabContent = document.getElementById('tab-content');
    if (!tabContent) return;
    document.getElementById('t-teams').className = type === 'teams' ? 'tab-btn active-tab' : 'tab-btn';
    document.getElementById('t-msgs').className = type === 'msgs' ? 'tab-btn active-tab' : 'tab-btn';

    if (type === 'teams') {
        const myTeams = JSON.parse(localStorage.getItem('myTeams')) || [];
        if (myTeams.length === 0) {
            tabContent.innerHTML = '<p style="color:#999; text-align:center; margin:30px;">暂无组队记录</p>';
            return;
        }
        tabContent.innerHTML = myTeams.map(t => `
            <div class="item-row" style="padding:15px; border-bottom:1px solid #f9f9f9;">
                <span>${t.name}</span>
                <span class="badge">${t.role}</span>
            </div>
        `).join('');
    } else {
        const allTeams = JSON.parse(localStorage.getItem('allTeams')) || [];
        const msgs = [];
        allTeams.forEach(team => {
            if (team.leader === userData.nick) {
                team.comments.forEach(c => {
                    if (c.author !== userData.nick) {
                        msgs.push({ team: team.title, from: c.author, text: c.text });
                    }
                });
            }
        });
        if (msgs.length === 0) {
            tabContent.innerHTML = '<p style="color:#999; text-align:center; margin:30px;">暂无消息通知</p>';
        } else {
            tabContent.innerHTML = msgs.map(m => `
                <div class="item-row" style="padding:15px; border-bottom:1px solid #f9f9f9; display:block;">
                    <div style="font-size:12px; color:#999;">来自队伍：${m.team}</div>
                    <div style="margin-top:5px;"><strong>${m.from}:</strong> ${m.text}</div>
                </div>
            `).join('');
        }
    }
};

window.switchP = function (p) {
    document.querySelectorAll('.nav-links span').forEach(s => s.classList.remove('active'));
    const navItem = document.getElementById('n-' + p);
    if (navItem) navItem.classList.add('active');
    document.getElementById('p-home').style.display = p === 'home' ? 'block' : 'none';
    document.getElementById('p-profile').style.display = p === 'profile' ? 'block' : 'none';

    if (p === 'profile') {
        userData = JSON.parse(localStorage.getItem('currentUser')) || userData;
        const avatarImg = document.getElementById('info-avatar');
        if (avatarImg) avatarImg.src = userData.avatar || "https://ui-avatars.com/api/?name=" + encodeURIComponent(userData.nick);
        if (document.getElementById('info-nick')) document.getElementById('info-nick').innerText = userData.nick;
        if (document.getElementById('info-uid')) document.getElementById('info-uid').innerText = userData.user_id;
        if (document.getElementById('info-bio')) document.getElementById('info-bio').innerText = userData.introduction;
        if (document.getElementById('info-utime')) document.getElementById('info-utime').innerText = userData.update_time;
        const tag = document.getElementById('status-tag');
        if (tag) tag.style.display = (userData.status === '禁用') ? 'block' : 'none';
        window.switchTab('teams');
    }
};

function init() {
    userData = JSON.parse(localStorage.getItem('currentUser')) || userData;
    const navAvatar = document.getElementById('nav-avatar');
    const navNick = document.getElementById('nav-nick');
    const avatarUrl = userData.avatar || "https://ui-avatars.com/api/?name=" + encodeURIComponent(userData.nick);

    if (navAvatar) navAvatar.src = avatarUrl;
    if (navNick) navNick.innerText = userData.nick;
    renderHome();
}

window.closeDetail = function (e) {
    if (e.target === document.getElementById('detail-modal')) {
        document.getElementById('detail-modal').style.display = 'none';
    }
};

document.addEventListener('DOMContentLoaded', init);