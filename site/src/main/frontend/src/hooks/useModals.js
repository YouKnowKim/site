import { atom, useRecoilState } from 'recoil';
import { useCallback, useEffect } from 'react'

// useModals 훅은 여러 군데에서 사용되어지기 때문에 이 훅을 사용하는 곳에서
// 리렌더링이 발생하는 것을 방지하기 위해 훅 내 함수들은 useCallBack을 이용해
// 메모이제이션 해주었다.

const modalOpenAtom = atom({
  key: 'modalOpenAtom',
  default: false,
});

const useModals = () => {

  useEffect(() => {
    const handleEscape = (e) => {
        if (e.key === 'Escape' && modalOpen) {
        closeModal();
        }
    };
    
    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [modalOpen, closeModal]);

  const [modalOpen, setModalOpen] = useRecoilState(modalOpenAtom);

  const openModal = useCallback(() => {
      setModalOpen(true);
  }, [setModalOpen]);

  const closeModal = useCallback(() => {
      setModalOpen(false);
  }, [setModalOpen]);

  return {
    modalOpen,
    openModal,
    closeModal,
  };
};

export default useModals;