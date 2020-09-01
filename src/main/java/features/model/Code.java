package features.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Entity
public class Code {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String codeList;

    @NotBlank
    private String code;

    public Code(Long id, @NotBlank String codeList, @NotBlank String code) {
        this.id = id;
        this.codeList = codeList;
        this.code = code;
    }

    public Code() {
    }

    public static CodeBuilder builder() {
        return new CodeBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public @NotBlank String getCodeList() {
        return this.codeList;
    }

    public @NotBlank String getCode() {
        return this.code;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCodeList(@NotBlank String codeList) {
        this.codeList = codeList;
    }

    public void setCode(@NotBlank String code) {
        this.code = code;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Code)) return false;
        final Code other = (Code) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$codeList = this.getCodeList();
        final Object other$codeList = other.getCodeList();
        if (this$codeList == null ? other$codeList != null : !this$codeList.equals(other$codeList)) return false;
        final Object this$code = this.getCode();
        final Object other$code = other.getCode();
        if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Code;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $codeList = this.getCodeList();
        result = result * PRIME + ($codeList == null ? 43 : $codeList.hashCode());
        final Object $code = this.getCode();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        return result;
    }

    public String toString() {
        return "Code(id=" + this.getId() + ", codeList=" + this.getCodeList() + ", code=" + this.getCode() + ")";
    }

    public static class CodeBuilder {
        private Long id;
        private @NotBlank String codeList;
        private @NotBlank String code;

        CodeBuilder() {
        }

        public Code.CodeBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public Code.CodeBuilder codeList(@NotBlank String codeList) {
            this.codeList = codeList;
            return this;
        }

        public Code.CodeBuilder code(@NotBlank String code) {
            this.code = code;
            return this;
        }

        public Code build() {
            return new Code(id, codeList, code);
        }

        public String toString() {
            return "Code.CodeBuilder(id=" + this.id + ", codeList=" + this.codeList + ", code=" + this.code + ")";
        }
    }
}
