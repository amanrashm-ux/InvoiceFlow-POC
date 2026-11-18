const API = '/api';

export async function getJson<T>(path: string): Promise<T> {
    const r = await fetch(`${API}${path}`);
    if (!r.ok) throw new Error(`Request failed (${r.status})`);
    return r.json();
}

export async function postJson<T>(path: string, body?: unknown): Promise<T> {
    const r = await fetch(`${API}${path}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: body === undefined ? undefined : JSON.stringify(body)
    });
    if (!r.ok) {
        const t = await r.text();
        throw new Error(t || `Request failed (${r.status})`);
    }
    return r.json();
}
