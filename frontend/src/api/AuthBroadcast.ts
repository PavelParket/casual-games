type BroadcastMessage =
    | { type: 'REFRESHED'; token: string }
    | { type: 'LOGGED_OUT' };

const CHANNEL_NAME = 'auth';

let channel: BroadcastChannel | null = null;

const getChannel = (): BroadcastChannel | null => {
    if (!channel && typeof BroadcastChannel !== 'undefined') {
        channel = new BroadcastChannel(CHANNEL_NAME);
    }
    return channel;
};

export const AuthBroadcast = {
    postRefreshed: (token: string): void => {
        getChannel()?.postMessage({ type: 'REFRESHED', token } satisfies BroadcastMessage);
    },

    postLoggedOut: (): void => {
        getChannel()?.postMessage({ type: 'LOGGED_OUT' } satisfies BroadcastMessage);
    },

    onMessage: (handler: (event: BroadcastMessage) => void): (() => void) => {
        const ch = getChannel();
        if (!ch) return () => { };

        const listener = (event: MessageEvent<BroadcastMessage>) => handler(event.data);
        ch.addEventListener('message', listener);
        return () => ch.removeEventListener('message', listener);
    },
};
