import { SECURITY_SERVICE_URL } from './ApiDictionary';
import axios from 'axios';

export const client = axios.create({
   baseURL: SECURITY_SERVICE_URL,
   withCredentials: true,
   headers: {
      'Content-Type': 'application/json',
   },
});
