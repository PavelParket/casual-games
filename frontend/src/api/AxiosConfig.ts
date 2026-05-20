import axios from 'axios';
import { API_GATEWAY_URL } from './ApiDictionary';

export const client = axios.create({
    baseURL: API_GATEWAY_URL,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});
