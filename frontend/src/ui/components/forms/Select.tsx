import { useState, useRef, useEffect } from "react";
import "../styles/select.css";

type SelectOption = {
    value: string;
    label: string;
};

type SelectProps = {
    options: SelectOption[];
    value?: string;
    onChange: (value: string) => void;
    placeholder?: string;
    searchable?: boolean;
    disabled?: boolean;
};

export function Select({
    options,
    value,
    onChange,
    placeholder = "Nothing chosen",
    searchable,
    disabled = false,
}: SelectProps) {
    const isSearchable = searchable === true;
    const [isOpen, setIsOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState("");
    const selectRef = useRef<HTMLDivElement>(null);

    const selectedOption = options.find((opt) => opt.value === value);
    const displayValue = selectedOption ? selectedOption.label : placeholder;

    const filteredOptions = isSearchable
        ? options.filter((opt) =>
            opt.label.toLowerCase().includes(searchQuery.toLowerCase())
        )
        : options;

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (
                selectRef.current &&
                !selectRef.current.contains(event.target as Node)
            ) {
                setIsOpen(false);
                setSearchQuery("");
            }
        };

        if (isOpen) {
            document.addEventListener("mousedown", handleClickOutside);
        }

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [isOpen]);

    const handleSelect = (optionValue: string) => {
        onChange(optionValue);
        setIsOpen(false);
        setSearchQuery("");
    };

    return (
        <div className="select-wrapper" ref={selectRef}>
            {isOpen && isSearchable ? (
                <div className="select-search-trigger">
                    <input
                        type="text"
                        className="select-search-input-trigger"
                        placeholder="Search..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        onClick={(e) => e.stopPropagation()}
                        autoFocus
                    />
                    <span className="select-arrow">▼</span>
                </div>
            ) : (
                <div
                    className={`select-trigger ${disabled ? "disabled" : ""} ${isOpen ? "open" : ""
                        }`}
                    onClick={() => !disabled && setIsOpen(!isOpen)}
                >
                    <span
                        className={`select-value ${!selectedOption ? "placeholder" : ""
                            }`}
                    >
                        {displayValue}
                    </span>
                    <span className="select-arrow">▼</span>
                </div>
            )}

            {isOpen && (
                <div className="select-dropdown">
                    <div className="select-options">
                        {filteredOptions.length > 0 ? (
                            filteredOptions.map((option) => (
                                <div
                                    key={option.value}
                                    className={`select-option ${value === option.value ? "selected" : ""
                                        }`}
                                    onClick={() => handleSelect(option.value)}
                                >
                                    {option.label}
                                </div>
                            ))
                        ) : (
                            <div className="select-option no-results">
                                No results found
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
