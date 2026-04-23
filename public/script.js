let userData = JSON.parse(localStorage.getItem('currentUser')) || {
    user_id: "ID_" + Math.floor(Math.random() * 1000000),
    nick: "新用户",
    avatar: "",
    gender: "男",
    age: 18,
    contact: "未填写",
    game_name: "未设定",
    game_rank: "",
    personality: "",
    playtime: "",
    matchneed: "",
    introduction: "这个同学很懒，什么都没写。",
    role: "普通用户",
    status: "正常",
    create_time: new Date().toLocaleDateString(),
    update_time: "从未更新"
};

async function renderHome() {
    const res = await fetch("http://localhost:8081/team/list");
    const result = await res.json();
    const list = result.data || [];

    const homeList = document.getElementById("home-list");
    if (!homeList) return;

    homeList.innerHTML = list.map(t => `
        <div class="team-card" onclick="window.showDetail(${t.id})">
            <div class="card-img">
                <img src="http://localhost:8081${t.avatar}">
            </div>
            <div class="card-body">
                <h3 style="margin:0 0 10px 0;">${t.title}</h3>
                <p style="font-size:13px; margin:5px 0;">
                    <span class="btn-s" style="background:var(--danger); padding:2px 6px;">队长</span> ${t.leader}
                </p>
                <p style="font-size:13px; margin:5px 0;">
                    <span class="btn-s" style="background:var(--success); padding:2px 6px;">需求</span> ${t.need}
                </p>
            </div>
        </div>
    `).join('');
}

window.showDetail = async function (id) {
    const res = await fetch(`http://localhost:8081/team/detail/${id}`);
    const result = await res.json();
    const t = result.data;
    if (!t) return alert("队伍不存在");

    const content = document.getElementById('detail-content');
    if (!content) return;

    const commentsHtml = t.comments?.length ? t.comments.map(c => `
        <div class="comment-item">
            <img class="comment-avatar" src="https://ui-avatars.com/api/?name=${encodeURIComponent(c.author)}">
            <div class="comment-content">
                <div class="comment-header">
                    <span class="comment-author">${c.author}</span>
                    <span class="comment-time">${c.time}</span>
                </div>
                <p class="comment-text">${c.text}</p>
            </div>
        </div>
    `).join('') : '<p style="text-align:center; color:#999;">暂无留言</p>';

    content.innerHTML = `
        <div class="detail-split">
            <img class="detail-cover" src="http://localhost:8081${t.avatar}">
            <div class="detail-info">
                <h2 style="margin:0 0 10px 0;">${t.title}</h2>
                <p><strong>队长：</strong>${t.leader}</p>
                <p style="color:var(--danger);"><strong>需求：</strong>${t.need}</p>
                <div style="background:#f9f9f9; padding:10px; border-radius:8px;">${t.desc}</div>
            </div>
        </div>
        <div class="comment-section">
            <h4>留言区 (${t.comments?.length || 0})</h4>
            <div class="comment-list">${commentsHtml}</div>
            <div class="comment-input-box">
                <input type="text" placeholder="评论功能请对接后端发表接口">
                <button class="btn-s" style="background:var(--primary);">发送</button>
            </div>
        </div>

        <button class="btn-s" style="background:var(--primary); width:100%; margin-top:15px;"
            onclick="applyJoinTeam(${t.id})">
            申请加入
        </button>
    `;
    document.getElementById('detail-modal').style.display = 'block';
};

async function applyJoinTeam(teamId) {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (!user) {
        alert("请先登录");
        return;
    }

    const res = await fetch("http://localhost:8081/team/apply", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            teamId: teamId,
            userId: user.user_id
        })
    });

    const result = await res.json();
    alert(result.msg);
}

window.closeDetail = function (e) {
    if (e.target === document.getElementById('detail-modal')) {
        document.getElementById('detail-modal').style.display = 'none';
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

        // ========== 修复：个人资料头像也拼接前缀 ==========
        let avatarUrl = userData.avatar || "";
        if (avatarUrl && !avatarUrl.startsWith("http")) {
            avatarUrl = "http://localhost:8081" + avatarUrl;
        }
        if (!avatarUrl) {
            avatarUrl = "https://ui-avatars.com/api/?name=" + encodeURIComponent(userData.nick);
        }

        document.getElementById('info-avatar').src = avatarUrl;
        document.getElementById('info-nick').innerText = userData.nick;
        document.getElementById('info-uid').innerText = userData.user_id;
        document.getElementById('info-bio').innerText = userData.introduction;
        document.getElementById('info-utime').innerText = userData.update_time;
    }
};

window.switchTab = async function (type) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active-tab'));
    document.getElementById('t-' + type).classList.add('active-tab');

    const tabContent = document.getElementById('tab-content');
    const user = JSON.parse(localStorage.getItem('currentUser'));

    if (type === 'teams') {
        try {
            const res = await fetch("http://localhost:8081/team/my/joined?userId=" + user.user_id);
            const result = await res.json();
            const list = result.data || [];

            if (list.length === 0) {
                tabContent.innerHTML = "<p>你还没有加入任何队伍</p>";
                return;
            }

            let html = "";
            list.forEach(team => {
                html += `
                <div style="border:1px solid #eee; border-radius:10px; padding:15px; margin-bottom:12px;">
                    <h3>${team.teamName}</h3>
                    <p>队长ID：${team.leaderId}</p>
                    <p>加入时间：${team.joinTime}</p>
                    <hr>
                    <h4>队伍成员（${team.members.length}人）</h4>
                `;

                team.members.forEach(m => {
                    html += `
                    <div style="padding:5px 0; font-size:14px;">
                        用户ID：${m.userId}｜加入时间：${m.joinTime}
                    </div>`;
                });

                html += `</div>`;
            });

            tabContent.innerHTML = html;
        } catch (e) {
            tabContent.innerHTML = "<p>加载失败</p>";
        }

    } else {
        const res = await fetch("http://localhost:8081/team/my/applies?leaderId=" + user.user_id);
        const result = await res.json();
        const list = result.data || [];

        if (list.length === 0) {
            tabContent.innerHTML = `<p style="text-align:center;">暂无申请消息</p>`;
            return;
        }

        tabContent.innerHTML = `
            <h4 style="margin-bottom:15px;">入队申请</h4>
            ${list.map(a => `
                <div style="padding:12px; border-bottom:1px solid #eee;">
                    <p>申请人ID：${a.userId}</p>
                    <p>申请时间：${new Date(a.applyTime).toLocaleString()}</p>
                    <p>状态：${a.status === 0 ? '待处理' :
                a.status === 1 ? '已同意' : '已拒绝'
            }</p>
                    ${a.status === 0 ? `
                        <div style="display:flex;gap:10px;margin-top:8px;">
                            <button class="btn-s" style="background:var(--success);flex:1;" onclick="agreeApply(${a.id})">同意</button>
                            <button class="btn-s" style="background:var(--danger);flex:1;" onclick="rejectApply(${a.id})">拒绝</button>
                        </div>
                    ` : ''}
                </div>
            `).join('')}
        `;
    }
};

async function agreeApply(id) {
    const res = await fetch("http://localhost:8081/team/agree?id=" + id, { method: "POST" });
    const r = await res.json();
    alert(r.msg);
    switchTab('msgs');
}

async function rejectApply(id) {
    const res = await fetch("http://localhost:8081/team/reject?id=" + id, { method: "POST" });
    const r = await res.json();
    alert(r.msg);
    switchTab('msgs');
}

async function init() {
    let localUser = JSON.parse(localStorage.getItem('currentUser')) || userData;
    userData = localUser;

    if (userData.user_id) {
        try {
            const res = await fetch("http://localhost:8081/user/info?userId=" + userData.user_id);
            const result = await res.json();
            if (result.data) {
                userData = result.data;
                localStorage.setItem('currentUser', JSON.stringify(userData));
            }
        } catch (e) {
            console.log("从数据库获取用户信息失败", e);
        }
    }

    // ========== 修复：顶部头像自动拼接前缀 ==========
    let avatarUrl = userData.avatar || "";
    if (avatarUrl && !avatarUrl.startsWith("http")) {
        avatarUrl = "http://localhost:8081" + avatarUrl;
    }
    if (!avatarUrl) {
        avatarUrl = "https://ui-avatars.com/api/?name=" + encodeURIComponent(userData.nick || "用户");
    }

    document.getElementById('nav-avatar').src = avatarUrl;
    document.getElementById('nav-nick').innerText = userData.nick;

    renderHome();
}

document.addEventListener('DOMContentLoaded', init);