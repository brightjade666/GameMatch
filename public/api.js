(function (window) {
    'use strict';

    const API_BASE = 'http://localhost:8081';
    const nativeFetch = window.fetch.bind(window);
    const PUBLIC_PATHS = new Set(['/login', '/register', '/refresh-token']);
    let refreshPromise = null;

    function readCurrentUser() {
        try {
            return JSON.parse(localStorage.getItem('currentUser') || '{}');
        } catch (error) {
            return {};
        }
    }

    function getAccessToken() {
        const user = readCurrentUser();
        return localStorage.getItem('accessToken') || user.accessToken || user.access_token || user.token || '';
    }

    function getRefreshToken() {
        const user = readCurrentUser();
        return localStorage.getItem('refreshToken') || user.refreshToken || user.refresh_token || '';
    }

    function saveTokenPair(data) {
        const accessToken = data.access_token || data.accessToken || data.token;
        const refreshToken = data.refresh_token || data.refreshToken;
        const user = readCurrentUser();

        if (accessToken) {
            localStorage.setItem('accessToken', accessToken);
            user.accessToken = accessToken;
            user.access_token = accessToken;
            user.token = accessToken;
        }
        if (refreshToken) {
            localStorage.setItem('refreshToken', refreshToken);
            user.refreshToken = refreshToken;
            user.refresh_token = refreshToken;
        }
        localStorage.setItem('currentUser', JSON.stringify(user));
    }

    function saveLoginSession(loginResult, profile) {
        const userId = loginResult.userId || loginResult.user_id || profile?.userId || profile?.user_id;
        const currentUser = {
            ...readCurrentUser(),
            ...(profile || {}),
            userId: userId,
            user_id: userId,
            username: loginResult.username || profile?.username,
            role: loginResult.role ?? profile?.role
        };
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        saveTokenPair(loginResult);
        return readCurrentUser();
    }

    function clearSession() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('currentUser');
    }

    function requestPath(input) {
        const url = typeof input === 'string' ? input : input.url;
        try {
            return new URL(url, window.location.href).pathname;
        } catch (error) {
            return '';
        }
    }

    function isBackendRequest(input) {
        const url = typeof input === 'string' ? input : input.url;
        try {
            return new URL(url, window.location.href).origin === new URL(API_BASE).origin;
        } catch (error) {
            return false;
        }
    }

    async function refreshTokens() {
        if (refreshPromise) return refreshPromise;
        const refreshToken = getRefreshToken();
        if (!refreshToken) return false;

        refreshPromise = (async function () {
            try {
                const response = await nativeFetch(API_BASE + '/refresh-token', {
                    method: 'POST',
                    headers: { 'X-Refresh-Token': refreshToken }
                });
                const result = await response.json();
                if (!response.ok || result.code !== 200) return false;
                saveTokenPair(result);
                return true;
            } catch (error) {
                return false;
            } finally {
                refreshPromise = null;
            }
        })();

        return refreshPromise;
    }

    function withAuthorization(input, init) {
        const options = { ...(init || {}) };
        const sourceHeaders = options.headers || (input instanceof Request ? input.headers : undefined);
        const headers = new Headers(sourceHeaders || {});
        const token = getAccessToken();
        if (token) headers.set('Authorization', 'Bearer ' + token);
        options.headers = headers;
        return options;
    }

    async function authenticatedFetch(input, init) {
        if (!isBackendRequest(input) || PUBLIC_PATHS.has(requestPath(input))) {
            return nativeFetch(input, init);
        }

        let response = await nativeFetch(input, withAuthorization(input, init));
        if (response.status !== 401 || requestPath(input) === '/logout') {
            return response;
        }

        if (await refreshTokens()) {
            response = await nativeFetch(input, withAuthorization(input, init));
            return response;
        }

        clearSession();
        if (!window.location.pathname.endsWith('/login.html')) {
            window.location.href = 'login.html';
        }
        return response;
    }

    async function logout() {
        try {
            await authenticatedFetch(API_BASE + '/logout', { method: 'POST' });
        } finally {
            clearSession();
        }
    }

    function assetUrl(path) {
        if (!path || /^https?:\/\//i.test(path)) return path || '';
        return API_BASE + (path.startsWith('/') ? path : '/' + path);
    }

    window.fetch = authenticatedFetch;
    window.GameMatchApi = {
        API_BASE,
        readCurrentUser,
        saveLoginSession,
        saveTokenPair,
        clearSession,
        refreshTokens,
        logout,
        assetUrl
    };
})(window);
