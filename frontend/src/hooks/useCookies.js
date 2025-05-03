import { useCookies } from 'react-cookie';

export function useUserCookies() {
    const [cookies, setCookie, removeCookie] = useCookies(['user']);

    const setUser = (userData) => {
        setCookie('user', userData, { path: '/' });
    };

    const removeUser = () => {
        removeCookie('user', { path: '/' });
    };

    return { cookies, setUser, removeUser };
}